package sharedUtils;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/**
 * This class represents a DBImporter which consumes messages from RabbitMQ
 * and inserts them into a PostgreSQL database.
 */
public class SharedUtils {

/**
 * Represents the name of the exchange used in RabbitMQ.
 */
private static final String EXCHANGE = "messages";

/**
 * Represents the type of exchange used in RabbitMQ.
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

/**
 * Counter for tracking the number of received requests.
 */
private static Counter receivedCounter;

/**
 * Channel for communication with RabbitMQ.
 */
private static Channel channel;

/**
 * Represents the name of the queue used in RabbitMQ for database import.
 */
private static String QUEUE_NAME;

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

}
