package lightBulb.jar;

import com.rabbitmq.client.ConnectionFactory;

public class LightBulbController {

    public static void main(String[] args) {
        System.out.println("Starting Program.");

        LightBulb lb = new LightBulb(State.ON);
        
        System.out.println(lb.toString());
        
        String rabbitMQ_host = "localhost";
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMQ_host);
        factory.setPort(15678);
        factory.setUsername("user1");
        factory.setPassword("MyPassword");
        
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();


        String variableName = "PATH"; // Example: retrieving the PATH variable

        // Retrieve the value of the specified environment variable
        String variableValue = System.getenv(variableName);

        // Check if the variable exists
        if (variableValue != null) {
            System.out.println("Value of " + variableName + ": " + variableValue);
        } else {
            System.out.println("Environment variable " + variableName + " not found");
        }
    
    }
}