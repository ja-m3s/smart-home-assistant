package lightBulbMonitor;

import java.io.IOException;

import org.json.JSONObject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class LightBulbMonitor {

    private final static String EXCHANGE="messages";
    private final static String EXCHANGE_TYPE = "fanout";
    
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;
    private String hostname;
    private String queue_name = "";

    public LightBulbMonitor(String rabbitmqHost, String rabbitmqPort, String rabbitmqUser, String rabbitmqPass, String hostname) {
        this.connectionFactory = createConnectionFactory(rabbitmqHost, rabbitmqPort, rabbitmqUser, rabbitmqPass);
        this.hostname = hostname;
    }

    private void setupExchange() throws IOException {
        this.channel.exchangeDeclare(EXCHANGE, EXCHANGE_TYPE);
    }

    private ConnectionFactory createConnectionFactory(String rabbitmqHost, String rabbitmqPort, String rabbitmqUser, String rabbitmqPass) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitmqHost);
        factory.setPort(Integer.parseInt(rabbitmqPort));
        factory.setUsername(rabbitmqUser);
        factory.setPassword(rabbitmqPass);
        return factory;
    }

    private void connectToRabbitMQ(int maxAttempts) {
        int attempts = 0;
        boolean connected = false;

        while (!connected && (maxAttempts == 0 || attempts < maxAttempts)) {
            try {
                this.connection = connectionFactory.newConnection();
                this.channel = connection.createChannel();
                connected = true;
                System.out.println("Connected to RabbitMQ");
            } catch (Exception e) {
                attempts++;
                System.out.println("Connection attempt #" + attempts + " failed. Retrying...");
                try {
                    Thread.sleep(1000); // Wait for 1 second before retrying
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!connected) {
            System.out.println("Failed to connect to RabbitMQ after " + attempts + " attempts.");
            System.exit(1);
        }
    }

    private void setupQueue() throws IOException {
        String queueName = channel.queueDeclare().getQueue();
        System.out.println("Created queue: "+queueName);
        channel.queueBind(queueName, EXCHANGE,"");
        this.queue_name = queueName;
      }

    private void sendMessage(String message) throws IOException, InterruptedException {
                this.channel.basicPublish(EXCHANGE, "", null, message.getBytes());
                System.out.println("Sent '" + message + "'");
                Thread.sleep(5000); // Sleep for 5 seconds
    }
    

    private static String retrieveEnvVariable(String variableName) {
        String variableValue = System.getenv(variableName);
        if (variableValue == null) {
            System.out.println("Environment variable " + variableName + " not found. Please set in system environment");
            System.exit(1);
        } else {
            System.out.println("Value of " + variableName + ": " + variableValue);
        }
        return variableValue;
    }

    private JSONObject createMessage(String target){
        JSONObject msg = new JSONObject();
        msg.put("hostname", this.hostname);
        msg.put("bulb_state", "triggered");
        msg.put("target", target);
        msg.put("sent_timestamp", System.currentTimeMillis());
        
        System.out.println("JSON message: " + msg);

        return msg;
    }

    private void consumeQueue() {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Received '" + message + "'");
            try {
                JSONObject msg = new JSONObject(message);
        
                String bulb_state = msg.getString("bulb_state");
                long sent_timestamp = msg.getLong("time_turned_on");
                long currentTimestamp = System.currentTimeMillis();
                long limit = 20_000; // 20 seconds in milliseconds
        
                System.out.println(bulb_state);
                System.out.println(sent_timestamp);
                System.out.println(limit);
        
                if (bulb_state.equals("on") && sent_timestamp + limit <= currentTimestamp) {
                    // Timestamp is 20 seconds or more in the past
                    System.out.println("Switching off light");
                    this.sendMessage(this.createMessage(msg.getString("hostname")).toString());
                } else {
                    System.out.println("Not switching off light");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        
        
        try {
            System.out.println("Consuming on"+this.queue_name);
            this.channel.basicConsume(this.queue_name, true, deliverCallback, consumerTag -> { });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * MAIN
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Starting LightBulbMonitor.");

        String rabbitmqHost = retrieveEnvVariable("RABBITMQ_HOST");
        String rabbitmqPort = retrieveEnvVariable("RABBITMQ_PORT");
        String rabbitmqUser = retrieveEnvVariable("RABBITMQ_USER");
        String rabbitmqPass = retrieveEnvVariable("RABBITMQ_PASS");
        String hostname = retrieveEnvVariable("HOSTNAME");

        LightBulbMonitor controller = new LightBulbMonitor(rabbitmqHost, rabbitmqPort, rabbitmqUser, rabbitmqPass,hostname);

        controller.connectToRabbitMQ(20); // Retry up to 20 times
        
        try {
            controller.setupExchange();
            controller.setupQueue();
            controller.consumeQueue();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
