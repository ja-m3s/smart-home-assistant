package lightBulbMonitor;
import sharedUtils.SharedUtils;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * The LightBulbMonitor class monitors the state of light bulbs and sends triggered messages accordingly.
 */
public class LightBulbMonitor {

    /**
     * Represents the name of the queue used for monitoring light bulbs.
     */
    private static final String QUEUE_NAME = "LIGHTBULBMONITOR";

    /**
     * Represents the time limit (in milliseconds) for the light bulb to remain on.
     */
    private static final long LIGHT_ON_LIMIT = 20_000; // 20 seconds

    /**
     * Regular expression pattern for matching hostnames of light bulbs.
     */
    private static final String LIGHT_BULB_HOSTNAME_REGEX = "light-bulb-\\d+";

    /**
     * Counter for tracking the number of received messages.
     */
    private static Counter receivedCounter;

    /**
     * Counter for tracking the number of sent messages.
     */
    private static Counter sentCounter;

    /**
     * Channel for communication with the message broker.
     */
    private static Channel channel;

    /**
     * The hostname of the current environment.
     */
    private static String hostname;

    private static final String COUNTER_SENT_NAME ="lightbulbmonitor_requests_sent_total";
    private static final String COUNTER_SENT_HELP ="Total Sent Messages";
    private static final String COUNTER_SENT_LABEL ="requests_sent";
    private static final String COUNTER_RECEIVED_NAME ="lightbulbmonitor_requests_received_total";
    private static final String COUNTER_RECEIVED_HELP ="Total Received Messages";
    private static final String COUNTER_RECEIVED_LABEL="requests_received";
    private static final Logger log = LoggerFactory.getLogger(LightBulbMonitor.class);

    /**
     * The main method.
     * @param args The command-line arguments.
     * @throws InterruptedException if the thread is interrupted.
     * @throws IOException if an I/O error occurs.
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        log.info("Starting LightBulbMonitor.%n");
        hostname = SharedUtils.getEnvVar("HOSTNAME");
        setupMetricServer();
        SharedUtils.startMetricsServer();
        setupQueue();
        consumeQueue();
    }

    /**
     * Sets up the metric server.
     */
    private static void setupMetricServer() {
        JvmMetrics.builder().register(); // initialize the out-of-the-box JVM metrics
        receivedCounter = Counter.builder().name(COUNTER_RECEIVED_NAME)
                .help(COUNTER_RECEIVED_HELP)
                .labelNames(COUNTER_RECEIVED_LABEL)
                .register();

        sentCounter = Counter.builder().name(COUNTER_SENT_NAME)
                .help(COUNTER_SENT_HELP)
                .labelNames(COUNTER_SENT_LABEL)
                .register();
    }

    /**
     * Consumes messages from the RabbitMQ queue.
     */
    private static void consumeQueue() {
        try {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                log.info("Received %s%n", message);
                receivedCounter.labelValues(COUNTER_RECEIVED_LABEL).inc();
                // Make json object from message
                JSONObject msg = new JSONObject(message);

                // Check message is from a lightbulb, if not, disregard it.
                String origin_hostname = msg.getString("hostname");
                log.info("Message from: %s%n", origin_hostname);
                if (!origin_hostname.matches(LIGHT_BULB_HOSTNAME_REGEX)) {
                    log.info("origin_hostname is not a light bulb. Disregarding message.");
                    return;
                }
                log.info("origin_hostname is a light bulb. Processing.");

                // Parse out the fields we need
                String bulb_state = msg.getString("bulb_state");
                long sent_timestamp = msg.getLong("time_turned_on");
                long currentTimestamp = System.currentTimeMillis();

                if (bulb_state.equals("ON") && sent_timestamp + LIGHT_ON_LIMIT <= currentTimestamp) {
                    // Timestamp is 20 seconds or more in the past
                    log.info("Switching off light");
                    JSONObject trigger_message = createTriggeredMessage(origin_hostname);
                    try {
                        sendMessage(trigger_message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    log.info("Not switching off light");
                }
            };

            log.info("Starting to consume %s%n", QUEUE_NAME);
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });

        } catch (Exception e) {
            throw new RuntimeException("Failed to consume messages from RabbitMQ queue", e);
        }
    }

    /**
     * Sends a message to RabbitMQ.
     * @param message The message to be sent.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the thread is interrupted.
     */
    private static void sendMessage(JSONObject message) throws IOException, InterruptedException {
        channel.basicPublish(SharedUtils.getExchangeName(), "", null, message.toString().getBytes(StandardCharsets.UTF_8));
        sentCounter.labelValues(COUNTER_SENT_LABEL).inc();
        log.info("Sent '" + message + "'");
    }

    /**
     * Creates a triggered message.
     * @param target The target hostname.
     * @return The JSON message.
     */
    private static JSONObject createTriggeredMessage(String target) {
        JSONObject msg = new JSONObject();
        msg.put("hostname", hostname);
        msg.put("bulb_state", "triggered");
        msg.put("target", target);
        msg.put("sent_timestamp", System.currentTimeMillis());

        log.info("JSON message: " + msg);

        return msg;
    }

    private static void setupQueue() throws IOException{
        channel = SharedUtils.setupRabbitMQConnection();
        channel.exchangeDeclare(SharedUtils.getExchangeName(), SharedUtils.getExchangeType());
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(QUEUE_NAME, SharedUtils.getExchangeName(), "");

    }
}
