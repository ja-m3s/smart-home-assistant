package lightBulb;

import java.io.IOException;
import org.json.JSONObject;

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

    final static String EXCHANGE = "messages";
    private final static String EXCHANGE_TYPE = "fanout";
    private final static Integer SEND_MESSAGE_POLL_TIME = 5000; // 5 seconds
    private static final Integer RABBITMQ_RETRY_INTERVAL = 1000; // 1 second
    protected static final String LIGHT_BULB_MONITOR_HOSTNAME_REGEX = "light-bulb-monitor-.+";;

    private final static String QUEUE_NAME = "";
    private LightBulb lightBulb;
    private ConnectionFactory connectionFactory;
    private Channel channel;
    private String hostname;

    /**
     * Constructs a LightBulbController object and initializes the light bulb state.
     */
    public LightBulbController() {
        this.lightBulb = new LightBulb();
    }

    /**
     * Connects to RabbitMQ server.
     */
    public void connectToRabbitMQ() {
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
                System.out.printf("Connection attempt #%s attempts. Retrying... %n", attempts);

                try {
                    Thread.sleep(RABBITMQ_RETRY_INTERVAL); // Wait for 1 second before retrying
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!connected) {
            System.out.printf("Failed to connect to RabbitMQ after %s attempts.%n", attempts);
            System.exit(1);
        }
    }

    /**
     * Sends a message to RabbitMQ.
     * 
     * @param message The message to be sent.
     * @throws IOException If an I/O error occurs.
     */
    void sendMessage(JSONObject message) throws IOException {
        channel.exchangeDeclare(EXCHANGE, EXCHANGE_TYPE);
        channel.queueBind(QUEUE_NAME, EXCHANGE, "");
        channel.basicPublish(EXCHANGE, QUEUE_NAME, null, message.toString().getBytes());
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
            System.out.printf("Environment variable %s not found. Please set in system environment %n", variableName);
            System.exit(1);
        } else {
            System.out.printf("Value of %s: %s%n", variableName, variableValue);
        }
        return variableValue;
    }

    /**
     * Creates a JSON message containing information about the light bulb state.
     * 
     * @return The JSON message.
     */
    JSONObject createMessage() {
        JSONObject msg = new JSONObject();
        msg.put("hostname", this.hostname);
        msg.put("bulb_state", this.lightBulb.getState());
        msg.put("sent_timestamp", System.currentTimeMillis());
        msg.put("time_turned_on", this.lightBulb.getTimeTurnedOn());

        System.out.println("JSON message: " + msg);

        return msg;
    }

    /**
     * Receives a message from RabbitMQ.
     */
    private void receiveMessage() throws IOException {
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE, "");

        System.out.println("Waiting for messages. To exit press Ctrl+C");

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                    byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received message: " + message);
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

        try {
            channel.basicConsume(QUEUE_NAME, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        controller.setHostname(retrieveEnvVariable("HOSTNAME"));
        controller.connectToRabbitMQ();
        try {
            controller.receiveMessage();
            while (true) {
                controller.sendMessage(controller.createMessage());
                Thread.sleep(SEND_MESSAGE_POLL_TIME); // Sleep for 5 seconds
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getHostname() {
        return this.hostname;
    }

    public String getQueueName() {
        return QUEUE_NAME;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
