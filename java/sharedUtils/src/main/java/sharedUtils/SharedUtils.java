package sharedUtils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

/**
 * This class represents a DBImporter which consumes messages from RabbitMQ
 * and inserts them into a PostgreSQL database.
 */
public class SharedUtils {

/**
 * Represents the delay (in milliseconds) for retrying operations.
 */
private static final int RETRY_DELAY_MILLIS = 1000;

/**
 * Represents the maximum number of attempts for retrying operations. 
 * A value of 0 indicates infinite retry attempts.
 */
private static final int RETRY_MAX_ATTEMPTS = 0; // forever

    /**
     * Retrieves an environment variable.
     * @param variableName The name of the environment variable
     * @return The value of the environment variable
     * @throws IllegalArgumentException if the environment variable is not found
     */
    public static String retrieveEnvVariable(String variableName) {
        String variableValue = System.getenv(variableName);
        if (variableValue == null) {
            throw new IllegalArgumentException(
                    "Environment variable " + variableName + " not found. Please set in system environment");
        }
        return variableValue;
    }

        /**
     * Connects to RabbitMQ server.
     * @return The channel.
     */
    @SuppressWarnings("all")
    public static Channel setupRabbitMQConnection() {
        for (int attempt = 1; RETRY_MAX_ATTEMPTS == 0 || attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(SharedUtils.retrieveEnvVariable("RABBITMQ_HOST"));
                factory.setPort(Integer.parseInt(SharedUtils.retrieveEnvVariable("RABBITMQ_PORT")));
                factory.setUsername(SharedUtils.retrieveEnvVariable("RABBITMQ_USER"));
                factory.setPassword(SharedUtils.retrieveEnvVariable("RABBITMQ_PASS"));
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

}
