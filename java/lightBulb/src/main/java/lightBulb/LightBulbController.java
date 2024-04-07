package lightBulb;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import sharedUtils.SharedUtils;
import org.json.JSONObject;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lightBulb.LightBulb.LightBulbState;

import com.rabbitmq.client.AMQP;

/**
 * The LightBulbController class manages the communication with RabbitMQ
 * and controls the state of the light bulb.
 */
public class LightBulbController {

/**
 * Represents the name of the queue used in messaging.
 * If it's "" it broadcasts to all queues on the exchange.
 */
private final static String QUEUE_NAME = "";

/**
 * Represents the time interval (in milliseconds) for sending messages.
 */
private final static Integer SEND_MESSAGE_POLL_TIME = 5000; // 5 seconds

/**
 * Regular expression pattern for matching hostnames of light bulb monitors.
 */
protected static final String LIGHT_BULB_MONITOR_HOSTNAME_REGEX = "light-bulb-monitor-.+";

/**
 * Represents the current state of the light bulb.
 */
private static LightBulb lightBulb;

/**
 * Channel for communication with the message broker.
 */
private static Channel channel;

/**
 * The hostname of the current environment.
 */
private static String hostname;

/**
 * Counter for tracking the number of received messages.
 */
private static Counter receivedCounter;

/**
 * Counter for tracking the number of sent messages.
 */
private static Counter sentCounter;

private static final String COUNTER_SENT_NAME ="lightbulb_requests_sent_total";
private static final String COUNTER_SENT_HELP ="Total Sent Messages";
private static final String COUNTER_SENT_LABEL ="requests_sent";
private static final String COUNTER_RECEIVED_NAME ="lightbulb_requests_received_total";
private static final String COUNTER_RECEIVED_HELP ="Total Received Messages";
private static final String COUNTER_RECEIVED_LABEL="requests_received";

    /**
     * The main method.
     * @param args The command-line arguments.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the thread is interrupted.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Starting Light Bulb.");
        lightBulb = new LightBulb();
        System.out.println(lightBulb.toString());
        hostname = SharedUtils.getEnvVar("HOSTNAME");
        channel = SharedUtils.setupRabbitMQConnection();
        setupQueue();
        setupMetricServer();
        SharedUtils.startMetricsServer();
        receiveMessage();
        while (true) {
            sendMessage(createMessage());
            Thread.sleep(SEND_MESSAGE_POLL_TIME); // Sleep for 5 seconds
        }
    }

    private static void setupMetricServer() {
        JvmMetrics.builder().register(); // initialize the out-of-the-box JVM metrics
        sentCounter = Counter.builder().name(COUNTER_SENT_NAME)
                .help(COUNTER_SENT_HELP)
                .labelNames(COUNTER_SENT_LABEL)
                .register();

        receivedCounter = Counter.builder().name(COUNTER_RECEIVED_NAME)
                .help(COUNTER_RECEIVED_HELP)
                .labelNames(COUNTER_RECEIVED_LABEL)
                .register();
    }

    /**
     * Sends a message to RabbitMQ.
     * @param message The message to be sent.
     * @throws IOException if an I/O error occurs.
     */
    private static void sendMessage(JSONObject message) throws IOException {
        // Only broadcast when lightbulb is on
        if (lightBulb.getState() == LightBulbState.ON) {
            channel.basicPublish(SharedUtils.getExchangeName(), QUEUE_NAME, null, message.toString().getBytes(StandardCharsets.UTF_8));
            sentCounter.labelValues(COUNTER_SENT_LABEL).inc();
            System.out.printf("Sent %s%n", message);
        }
    }

    /**
     * Creates a JSON message containing information about the light bulb state.
     * @return The JSON message.
     */
    private static JSONObject createMessage() {
        JSONObject msg = new JSONObject();
        msg.put("hostname", hostname);
        msg.put("bulb_state", lightBulb.getState());
        msg.put("sent_timestamp", System.currentTimeMillis());
        msg.put("time_turned_on", lightBulb.getTimeTurnedOn());

        System.out.println("JSON message: " + msg);

        return msg;
    }

    /**
     * Receives a message from RabbitMQ.
     * @throws IOException if an I/O error occurs.
     */
    private static void receiveMessage() throws IOException {
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                    byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received message: " + message);
                receivedCounter.labelValues(COUNTER_RECEIVED_LABEL).inc();
                // Make json object from message
                JSONObject msg = new JSONObject(message);

                // Check message is from a lightbulb, if not, disregard it.
                String origin_hostname = msg.getString("hostname");
                System.out.printf("Message from: %s%n", origin_hostname);
                if (!origin_hostname.matches(LIGHT_BULB_MONITOR_HOSTNAME_REGEX)) {
                    System.out.println("origin_hostname is not a light bulb monitor. Disregarding message.");
                    return;
                }
                System.out.println("origin_hostname is a light bulb monitor. Processing.");
                String target = msg.getString("target");

                // is message for this light
                if (target.equals(hostname)) {
                    System.out.println("Switching off light");
                    lightBulb.setState(LightBulbState.OFF);
                }
            }
        };

        System.out.println("Waiting for messages. To exit press Ctrl+C");

        try {
            channel.basicConsume(QUEUE_NAME, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setupQueue() throws IOException{
        channel.exchangeDeclare(SharedUtils.getExchangeName(), SharedUtils.getExchangeType());
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(QUEUE_NAME, SharedUtils.getExchangeName(), "");
    }
}
