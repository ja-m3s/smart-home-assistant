package lightBulb;

import java.io.IOException;
import org.json.JSONObject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class LightBulbController {

    private final static String EXCHANGE="messages";
    private final static String EXCHANGE_TYPE = "fanout";
    
    private final String queue_name = "";
    private LightBulb lightBulb;
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;
    private String hostname;

    public LightBulbController(String rabbitmqHost, String rabbitmqPort, String rabbitmqUser, String rabbitmqPass, String hostname) {
        this.lightBulb = new LightBulb(State.ON);
        this.connectionFactory = createConnectionFactory(rabbitmqHost, rabbitmqPort, rabbitmqUser, rabbitmqPass);
        this.hostname = hostname;
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
      //String queueName = channel.queueDeclare().getQueue();
      channel.queueBind(this.queue_name, EXCHANGE,"");
    }

    private void setupExchange() throws IOException {
        this.channel.exchangeDeclare(EXCHANGE, EXCHANGE_TYPE);
    }

    private void sendMessage(String message) throws IOException, InterruptedException {
                this.channel.basicPublish(EXCHANGE, queue_name, null, message.getBytes());
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

    private JSONObject createMessage(){
        JSONObject msg = new JSONObject();
        msg.put("hostname", this.hostname);
        msg.put("bulb_state", this.lightBulb.getState());
        msg.put("sent_timestamp", System.currentTimeMillis());
        msg.put("time_turned_on", this.lightBulb.getTimeTurnedOn());

        System.out.println("JSON message: " + msg);

        return msg;
    }

    /**
     * MAIN
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Starting Light Bulb.");

        String rabbitmqHost = retrieveEnvVariable("RABBITMQ_HOST");
        String rabbitmqPort = retrieveEnvVariable("RABBITMQ_PORT");
        String rabbitmqUser = retrieveEnvVariable("RABBITMQ_USER");
        String rabbitmqPass = retrieveEnvVariable("RABBITMQ_PASS");
        String hostname = retrieveEnvVariable("HOSTNAME");

        LightBulbController controller = new LightBulbController(rabbitmqHost, rabbitmqPort, rabbitmqUser, rabbitmqPass,hostname);
        System.out.println(controller.lightBulb.toString());

        controller.connectToRabbitMQ(20); // Retry up to 3 times
        
        try {
            //controller.setupQueue();
            controller.setupExchange();
            while (true) {
                controller.sendMessage(controller.createMessage().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
