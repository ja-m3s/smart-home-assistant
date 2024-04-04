package lightBulb;

import java.io.IOException;
import org.json.JSONObject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * The LightBulbController class manages the communication with RabbitMQ 
 * and controls the state of the light bulb.
 */
public class LightBulbController {

    private final static String EXCHANGE = "messages";
    private final static String EXCHANGE_TYPE = "fanout";
    private final static Integer SEND_MESSAGE_POLL_TIME = 5000 ; //5 seconds
    private static final Integer RABBITMQ_RETRY_INTERVAL = 1000; //1 second

    private final String queue_name = "";
    private LightBulb lightBulb;
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;
    private String hostname;

    /**
     * Constructs a LightBulbController object and initializes the light bulb state.
     */
    public LightBulbController() {
        this.lightBulb = new LightBulb();
        this.hostname = retrieveEnvVariable("HOSTNAME");
        connectToRabbitMQ();
    }

    /**
     * Connects to RabbitMQ server.
     */
    private void connectToRabbitMQ() {
        int attempts = 0;
        boolean connected = false;
        int maxAttempts = 0;

        String rabbitmqHost = retrieveEnvVariable("RABBITMQ_HOST");
        String rabbitmqPort = retrieveEnvVariable("RABBITMQ_PORT");
        String rabbitmqUser = retrieveEnvVariable("RABBITMQ_USER");
        String rabbitmqPass = retrieveEnvVariable("RABBITMQ_PASS");

        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitmqHost);
        connectionFactory.setPort(Integer.parseInt(rabbitmqPort));
        connectionFactory.setUsername(rabbitmqUser);
        connectionFactory.setPassword(rabbitmqPass);

        while (!connected && (maxAttempts == 0 || attempts < maxAttempts)) {
            try {
                this.channel = connectionFactory.newConnection().createChannel();
                connected = true;
                System.out.println("Connected to RabbitMQ");
            } catch (Exception e) {
                attempts++;
                System.out.printf("Connection attempt #%s attempts. Retrying... %n",attempts);

                try {
                    Thread.sleep(RABBITMQ_RETRY_INTERVAL); // Wait for 1 second before retrying
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!connected) {
            System.out.printf("Failed to connect to RabbitMQ after %s attempts.%n",attempts);
            System.exit(1);
        }
    }

    /**
     * Sends a message to RabbitMQ.
     * 
     * @param message The message to be sent.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException If the thread is interrupted while sleeping.
     */
    private void sendMessage(JSONObject message) throws IOException, InterruptedException {
        channel.queueBind(this.queue_name, EXCHANGE, "");
        channel.exchangeDeclare(EXCHANGE, EXCHANGE_TYPE);
        channel.basicPublish(EXCHANGE, queue_name, null, message.toString().getBytes());
        System.out.printf("Sent %s%n", message);
    }

    /**
     * Retrieves the value of an environment variable.
     * 
     * @param variableName The name of the environment variable.
     * @return The value of the environment variable.
     */
    private static String retrieveEnvVariable(String variableName) {
        String variableValue = System.getenv(variableName);
        if (variableValue == null) {
            System.out.printf("Environment variable %s not found. Please set in system environment %n",variableName);
            System.exit(1);
        } else {
            System.out.printf("Value of %s: %s", variableName,variableValue);
        }
        return variableValue;
    }

    /**
     * Creates a JSON message containing information about the light bulb state.
     * 
     * @return The JSON message.
     */
    private JSONObject createMessage() {
        JSONObject msg = new JSONObject();
        msg.put("hostname", this.hostname);
        msg.put("bulb_state", this.lightBulb.getState());
        msg.put("sent_timestamp", System.currentTimeMillis());
        msg.put("time_turned_on", this.lightBulb.getTimeTurnedOn());

        System.out.println("JSON message: " + msg);

        return msg;
    }

    /**
     * The main method.
     * 
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        System.out.println("Starting Light Bulb.");
        LightBulbController controller = new LightBulbController();
        System.out.println(controller.lightBulb.toString());
        
        try {
            while (true) {
                controller.sendMessage(controller.createMessage());
                Thread.sleep(SEND_MESSAGE_POLL_TIME); // Sleep for 5 seconds
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
