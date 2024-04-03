package dbImporter;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class DBImporter {

    private static String RABBITMQ_HOST;
    private static String RABBITMQ_PORT;
    private static String RABBITMQ_USER;
    private static String RABBITMQ_PASS;
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) {     
        System.out.println("Starting Light Bulb.");

        //Set environment variables
        RABBITMQ_HOST=retrieveEnvVariable("RABBITMQ_HOST");
        RABBITMQ_PORT=retrieveEnvVariable("RABBITMQ_PORT");
        RABBITMQ_USER=retrieveEnvVariable("RABBITMQ_USER");
        RABBITMQ_PASS=retrieveEnvVariable("RABBITMQ_PASS");

        
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);
        factory.setPort(Integer.parseInt(RABBITMQ_PORT));
        factory.setUsername(RABBITMQ_USER);
        factory.setPassword(RABBITMQ_PASS);
        
        Connection connection;
        Channel channel;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "Hello World!";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        } catch (Exception e) {
            System.out.println("Can't connect...night night.");
            System.exit(1);
        }
    }

    public static String retrieveEnvVariable(String variableName) {
        String variableValue = System.getenv(variableName);
        if (variableValue == null) {
            System.out.println("Environment variable " + variableName + " not found. Please set in system environment");
            System.exit(1);
        } else {
            System.out.println("Value of " + variableName + ": " + variableValue);
        }
        return variableValue;
    }
    
}   
