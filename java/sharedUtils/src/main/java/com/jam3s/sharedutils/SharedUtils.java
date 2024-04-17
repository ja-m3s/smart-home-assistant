package com.jam3s.sharedutils;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;

/**
 * This class represents a selection of common functions used by the Java apps.
 */
public final class SharedUtils {
    /**
     * Channel for communication with the message broker.
     */
    private static Channel channel;

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

    /**
     * sl4f logger.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(SharedUtils.class);

    /**
     * Private constructor.
     */
    private SharedUtils() {
    }

    /**
     * Retrieves an environment variable.
     *
     * @param variableName The name of the environment variable
     * @return The value of the environment variable
     * @throws IllegalArgumentException if the environment variable is not found
     */
    public static String getEnvVar(final String variableName) {
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
     */
    @SuppressWarnings("all")
    public static void setupRabbitMQConnection() {
        for (int attempt = 1; RETRY_MAX_ATTEMPTS == 0 || attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(SharedUtils.getEnvVar("RABBITMQ_HOST"));
                factory.setPort(Integer.parseInt(SharedUtils.getEnvVar("RABBITMQ_PORT")));
                factory.setUsername(SharedUtils.getEnvVar("RABBITMQ_USER"));
                factory.setPassword(SharedUtils.getEnvVar("RABBITMQ_PASS"));
                channel = factory.newConnection().createChannel();
                break;
            } catch (Exception e) {
                LOG.info("Failed to connect to RabbitMQ on attempt " + attempt + ". Retrying...");
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
    }

    /**
     * Sets up the RabbitMQ queue.
     * @param queueName Name of queue to setup.
     * @throws IOException if channel isn't setup.
     */
    public static void setupQueue(final String queueName) throws IOException {
        channel.exchangeDeclare(SharedUtils.getExchangeName(), SharedUtils.getExchangeType());
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, SharedUtils.getExchangeName(), "");
    }

    /**
     * Starts the metrics server.
     */
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

    /**
     * Gets the exchange name.
     *
     * @return the name of the exchange.
     */
    public static String getExchangeName() {
        return EXCHANGE_NAME;
    }

    /**
     * Gets the exchange type.
     *
     * @return the type of the exchange.
     */
    public static String getExchangeType() {
        return EXCHANGE_TYPE;
    }

    /**
     * Gets the RabbitMQ channel.
     *
     * @return the channel.
     */
    public static Channel getChannel() {
        return channel;
    }

    /**
     * Sets the RabbitMQ channel.
     *
     * @param newChannel a new channel.
     */
    public static void setChannel(final Channel newChannel) {
        channel = newChannel;
    }
}
