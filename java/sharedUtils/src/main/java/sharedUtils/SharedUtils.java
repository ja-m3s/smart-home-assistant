package sharedUtils;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;

/**
 * This class represents a DBImporter which consumes messages from RabbitMQ
 * and inserts them into a PostgreSQL database.
 */
public class SharedUtils {
    /**
     * Represents the name of the exchange used in messaging.
     */
    private static final String EXCHANGE_NAME = "messages";

    /**
     * Represents the type of exchange used in messaging.
     */
    private static final String EXCHANGE_TYPE = "fanout";
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
     * Represents the port number for the metrics server.
     */
    private static final int METRICS_SERVER_PORT = 9400;

    private static final Logger log = LoggerFactory.getLogger(SharedUtils.class);

    /**
     * Retrieves an environment variable.
     * 
     * @param variableName The name of the environment variable
     * @return The value of the environment variable
     * @throws IllegalArgumentException if the environment variable is not found
     */
    public static String getEnvVar(String variableName) {
        String variableValue = System.getenv(variableName);
        if (variableValue == null) {
            throw new IllegalArgumentException(
                    "Environment variable " + variableName + " not found. Please set in system environment");
        }
        return variableValue;
    }

    /**
     * Connects to RabbitMQ server.
     * 
     * @return The channel.
     */
    @SuppressWarnings("all")
    public static Channel setupRabbitMQConnection() {
        for (int attempt = 1; RETRY_MAX_ATTEMPTS == 0 || attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(SharedUtils.getEnvVar("RABBITMQ_HOST"));
                factory.setPort(Integer.parseInt(SharedUtils.getEnvVar("RABBITMQ_PORT")));
                factory.setUsername(SharedUtils.getEnvVar("RABBITMQ_USER"));
                factory.setPassword(SharedUtils.getEnvVar("RABBITMQ_PASS"));
                return factory.newConnection().createChannel();
            } catch (Exception e) {
                log.info("Failed to connect to RabbitMQ on attempt #%d. Retrying...%n", attempt);
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

    public static void startMetricsServer() {
        // Start HTTP server for Prometheus metrics
        Thread serverThread = new Thread(() -> {
            try {
                @SuppressWarnings("unused")
                HTTPServer server = HTTPServer.builder()
                        .port(METRICS_SERVER_PORT)
                        .buildAndStart();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

    public static String getExchangeName() {
        return EXCHANGE_NAME;
    }

    public static String getExchangeType() {
        return EXCHANGE_TYPE;
    }
}
