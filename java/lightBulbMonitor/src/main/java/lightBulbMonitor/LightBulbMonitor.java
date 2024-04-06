package lightBulbMonitor;
import java.io.IOException;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;
import org.json.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class LightBulbMonitor {

    private static final String EXCHANGE = "messages";
    private static final String EXCHANGE_TYPE = "fanout";
    private static final String QUEUE_NAME = "LIGHTBULBMONITOR";
    private static final int RETRY_DELAY_MILLIS = 1000;
    private static final long LIGHT_ON_LIMIT = 20_000; //20 seconds
    private static final String LIGHT_BULB_HOSTNAME_REGEX="light-bulb-\\d+";
    private final Counter requestsReceivedTotal = Counter.build()
    .name("lightbulbmonitor_requests_received_total")
    .help("Total number of received requests.")
    .register();
    private final Counter requestsSentTotal = Counter.build()
    .name("lightbulbmonitor_requests_sent_total")
    .help("Total number of sent requests.")
    .register();
    public Channel channel;
    private String hostname;

    public LightBulbMonitor() {
       // this.channel = setupRabbitMQConnection();
    }

    public Channel setupRabbitMQConnection() {
        int maxRetries = getMaxRetries();
        for (int attempt = 1; maxRetries == 0 || attempt <= maxRetries; attempt++) {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(retrieveEnvVariable("RABBITMQ_HOST"));
                factory.setPort(Integer.parseInt(retrieveEnvVariable("RABBITMQ_PORT")));
                factory.setUsername(retrieveEnvVariable("RABBITMQ_USER"));
                factory.setPassword(retrieveEnvVariable("RABBITMQ_PASS"));
                return factory.newConnection().createChannel();
            } catch (Exception e) {
                System.out.printf("Failed to connect to RabbitMQ on attempt #%d. Retrying...%n", attempt);
                if (maxRetries != 0 && attempt == maxRetries) {
                    throw new RuntimeException("Failed to connect to RabbitMQ after multiple attempts.", e);
                }
                try {
                    Thread.sleep(RETRY_DELAY_MILLIS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return null; // Unreachable code, added to satisfy compiler
    }

    private int getMaxRetries() {
        String maxRetriesStr = System.getenv("MAX_CONNECTION_RETRIES");
        if (maxRetriesStr != null) {
            try {
                return Integer.parseInt(maxRetriesStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid value for MAX_CONNECTION_RETRIES. Using default value.");
            }
        }
        // Default to infinite retries if MAX_CONNECTION_RETRIES is not set or invalid
        return 0;
    }

    public JSONObject createTriggeredMessage(String target){
        JSONObject msg = new JSONObject();
        msg.put("hostname", hostname);
        msg.put("bulb_state", "triggered");
        msg.put("target", target);
        msg.put("sent_timestamp", System.currentTimeMillis());
        
        System.out.println("JSON message: " + msg);

        return msg;
    }

    public void consumeQueue() {
        try {
            channel.exchangeDeclare(EXCHANGE, EXCHANGE_TYPE);
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE, "");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.printf("Received %s%n", message);
                requestsReceivedTotal.inc();
                // Make json object from message
                JSONObject msg = new JSONObject(message);

                // Check message is from a lightbulb, if not, disregard it.
                String origin_hostname = msg.getString("hostname");
                System.out.printf("Message from: %s%n", origin_hostname);
                if (! origin_hostname.matches(LIGHT_BULB_HOSTNAME_REGEX)) {
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
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(JSONObject message) throws IOException, InterruptedException {
        this.channel.basicPublish(EXCHANGE, "", null, message.toString().getBytes());
        requestsSentTotal.inc();
        System.out.println("Sent '" + message + "'");
}

    private static String retrieveEnvVariable(String variableName) {
        String variableValue = System.getenv(variableName);
        if (variableValue == null) {
            throw new IllegalArgumentException("Environment variable " + variableName + " not found. Please set in system environment");
        }
        return variableValue;
    }

    public static void main(String[] args) {
        System.out.printf("Starting DBImporter.%n");
        LightBulbMonitor monitor = new LightBulbMonitor();
        try {
            HTTPServer metrics_server = new HTTPServer(8080);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        monitor.setupRabbitMQConnection();
        monitor.setHostname(retrieveEnvVariable("HOSTNAME"));
        monitor.consumeQueue();
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setHostname(String hostname){
        this.hostname = hostname;
    }
}


   
