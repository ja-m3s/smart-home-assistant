package lightBulb;

import java.io.IOException;
import org.json.JSONObject;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lightBulb.LightBulb.LightBulbState;
import com.rabbitmq.client.AMQP;

/**
 * The LightBulbController class manages the communication with RabbitMQ 
 * and controls the state of the light bulb.
 */
public class LightBulbController {

    private final static String EXCHANGE = "messages";
    private final static String EXCHANGE_TYPE = "fanout";
    private final static String QUEUE_NAME = "";
    private final static Integer SEND_MESSAGE_POLL_TIME = 5000; // 5 seconds
    protected static final String LIGHT_BULB_MONITOR_HOSTNAME_REGEX = "light-bulb-monitor-.+";
    private static final int RETRY_DELAY_MILLIS = 1000;
    private static final int RETRY_MAX_ATTEMPTS = 0; //forever
    private static final int METRICS_SERVER_PORT = 9400;
    private static LightBulb lightBulb;
    private static Channel mqchannel;
    private static String hostname;
    private static Counter receivedCounter;
    private static Counter sentCounter;

    /**
     * The main method.
     * 
     * @param args The command-line arguments.
     * @throws IOException 
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Starting Light Bulb.");
        lightBulb = new LightBulb();
        System.out.println(lightBulb.toString());
        hostname = (retrieveEnvVariable("HOSTNAME"));
        mqchannel = setupRabbitMQConnection();
        setupMetricServer();
        receiveMessage();
        while (true) {
            sendMessage(createMessage());
            Thread.sleep(SEND_MESSAGE_POLL_TIME); // Sleep for 5 seconds
        }
        
    }
    @SuppressWarnings("unused")
    private static void setupMetricServer(){
        JvmMetrics.builder().register(); // initialize the out-of-the-box JVM metrics
        sentCounter = Counter.builder().name("lightbulb_requests_received_total")
            .help("Total number of sent requests")
            .labelNames("requests_sent")
            .register();
        sentCounter.labelValues("requests_sent").inc();

        receivedCounter = Counter.builder().name("lightbulb_requests_received_total")
        .help("Total number of received requests")
        .labelNames("requests_received")
        .register();
        receivedCounter.labelValues("requests_received").inc();

        Thread serverThread = new Thread(() -> {
            try {
                HTTPServer server = HTTPServer.builder()
                .port(METRICS_SERVER_PORT)
                .buildAndStart(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

    protected static String retrieveEnvVariable(String variableName) {
        String variableValue = System.getenv(variableName);
        if (variableValue == null) {
            throw new IllegalArgumentException(
                    "Environment variable " + variableName + " not found. Please set in system environment");
        }
        return variableValue;
    }

    /**
     * Connects to RabbitMQ server.
     */
    @SuppressWarnings("all")
    private static Channel setupRabbitMQConnection() {
    for (int attempt = 1; RETRY_MAX_ATTEMPTS == 0 || attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(retrieveEnvVariable("RABBITMQ_HOST"));
            factory.setPort(Integer.parseInt(retrieveEnvVariable("RABBITMQ_PORT")));
            factory.setUsername(retrieveEnvVariable("RABBITMQ_USER"));
            factory.setPassword(retrieveEnvVariable("RABBITMQ_PASS"));
            return factory.newConnection().createChannel();
        } catch (Exception e) {
            System.out.printf("Failed to connect to RabbitMQ on attempt #%d. Retrying...%n", attempt);
            if (attempt == RETRY_MAX_ATTEMPTS && RETRY_MAX_ATTEMPTS != 0) {
                throw new RuntimeException("Failed to connect to RabbitMQ after multiple attempts.", e);
            }
            try {
                Thread.sleep(RETRY_DELAY_MILLIS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
    return null;
}

    /**
     * Sends a message to RabbitMQ.
     * 
     * @param message The message to be sent.
     * @throws IOException If an I/O error occurs.
     */
    private static void sendMessage(JSONObject message) throws IOException {
        mqchannel.exchangeDeclare(EXCHANGE, EXCHANGE_TYPE);
        mqchannel.queueBind(QUEUE_NAME, EXCHANGE, "");
        mqchannel.basicPublish(EXCHANGE, QUEUE_NAME, null, message.toString().getBytes());
        receivedCounter.labelValues("requests_sent").inc();
        System.out.printf("Sent %s%n", message);
    }

    /**
     * Creates a JSON message containing information about the light bulb state.
     * 
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
     */
    private static void receiveMessage() throws IOException {

        DefaultConsumer consumer = new DefaultConsumer(mqchannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                    byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received message: " + message);
                 receivedCounter.labelValues("requests_received").inc();
                // Make json object from message
                JSONObject msg = new JSONObject(message);

                // Check message is from a lightbulb, if not, disregard it.
                String origin_hostname = msg.getString("hostname");
                System.out.printf("Message from: %s%n", origin_hostname);
                if (! origin_hostname.matches(LIGHT_BULB_MONITOR_HOSTNAME_REGEX)) {
                    System.out.println("origin_hostname is not a light bulb monitor. Disregarding message.");
                    return;
                }
                System.out.println("origin_hostname is a light bulb monitor. Processing.");
                String target = msg.getString("target");

                //is message for this light
                if (target.equals(hostname)) {
                    System.out.println("Switching off light");
                    lightBulb.setState(LightBulbState.OFF);
                }
            }
        };
        mqchannel = setupRabbitMQConnection();
        mqchannel.exchangeDeclare(EXCHANGE, EXCHANGE_TYPE);
        mqchannel.queueDeclare(QUEUE_NAME, false, false, false, null);
        mqchannel.queueBind(QUEUE_NAME, EXCHANGE, "");
        System.out.println("Waiting for messages. To exit press Ctrl+C");

        try {
            mqchannel.basicConsume(QUEUE_NAME, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
