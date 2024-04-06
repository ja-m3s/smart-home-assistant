package lightBulbMonitor;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;

import org.json.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class LightBulbMonitor {

    private static final String EXCHANGE = "messages";
    private static final String EXCHANGE_TYPE = "fanout";
    private static final String QUEUE_NAME = "LIGHTBULBMONITOR";
    private static final long LIGHT_ON_LIMIT = 20_000; // 20 seconds
    private static final String LIGHT_BULB_HOSTNAME_REGEX = "light-bulb-\\d+";
    private static final int RETRY_DELAY_MILLIS = 1000;
    private static final int RETRY_MAX_ATTEMPTS = 0; // forever
    private static final int METRICS_SERVER_PORT = 9400;
    private static Counter receivedCounter;
    private static Counter sentCounter;
    private static Channel mqchannel;
    private static String hostname;

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.printf("Starting LightBulbMonitor.%n");
        hostname = retrieveEnvVariable("HOSTNAME");
        setupMetricServer();
        consumeQueue();
    }

    protected static String retrieveEnvVariable(String variableName) {
        String variableValue = System.getenv(variableName);
        if (variableValue == null) {
            throw new IllegalArgumentException(
                    "Environment variable " + variableName + " not found. Please set in system environment");
        }
        return variableValue;
    }

    @SuppressWarnings("unused")
    private static void setupMetricServer() {
        JvmMetrics.builder().register(); // initialize the out-of-the-box JVM metrics
        receivedCounter = Counter.builder().name("lightbulbmonitor_requests_received_total")
                .help("Total number of received requests")
                .labelNames("requests_received")
                .register();

        sentCounter = Counter.builder().name("lightbulbmonitor_requests_sent_total")
                .help("Total number of sent requests")
                .labelNames("requests_sent")
                .register();

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

    private static void consumeQueue() {
        try {
            mqchannel = setupRabbitMQConnection();
            mqchannel.exchangeDeclare(EXCHANGE, EXCHANGE_TYPE);
            mqchannel.queueDeclare(QUEUE_NAME, false, false, false, null);
            mqchannel.queueBind(QUEUE_NAME, EXCHANGE, "");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.printf("Received %s%n", message);
                receivedCounter.labelValues("requests_received").inc();
                // Make json object from message
                JSONObject msg = new JSONObject(message);

                // Check message is from a lightbulb, if not, disregard it.
                String origin_hostname = msg.getString("hostname");
                System.out.printf("Message from: %s%n", origin_hostname);
                if (!origin_hostname.matches(LIGHT_BULB_HOSTNAME_REGEX)) {
                    System.out.println("origin_hostname is not a light bulb. Disregarding message.");
                    return;
                }
                System.out.println("origin_hostname is a light bulb. Processing.");

                // Parse out the fields we need
                String bulb_state = msg.getString("bulb_state");
                long sent_timestamp = msg.getLong("time_turned_on");
                long currentTimestamp = System.currentTimeMillis();

                if (bulb_state.equals("on") && sent_timestamp + LIGHT_ON_LIMIT <= currentTimestamp) {
                    // Timestamp is 20 seconds or more in the past
                    System.out.println("Switching off light");
                    JSONObject trigger_message = createTriggeredMessage(origin_hostname);
                    try {
                        sendMessage(trigger_message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Not switching off light");
                }
            };

            System.out.printf("Starting to consume %s%n", QUEUE_NAME);
            mqchannel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });

        } catch (Exception e) {
            throw new RuntimeException("Failed to consume messages from RabbitMQ queue", e);
        }
    }

    private static void sendMessage(JSONObject message) throws IOException, InterruptedException {
        mqchannel.basicPublish(EXCHANGE, "", null, message.toString().getBytes());
        sentCounter.labelValues("requests_sent").inc();
        System.out.println("Sent '" + message + "'");
    }

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

    private static JSONObject createTriggeredMessage(String target) {
        JSONObject msg = new JSONObject();
        msg.put("hostname", hostname);
        msg.put("bulb_state", "triggered");
        msg.put("target", target);
        msg.put("sent_timestamp", System.currentTimeMillis());

        System.out.println("JSON message: " + msg);

        return msg;
    }

}