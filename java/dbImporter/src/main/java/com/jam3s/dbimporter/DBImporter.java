package com.jam3s.dbimporter;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jam3s.sharedutils.SharedUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * This class represents a DBImporter which consumes messages from RabbitMQ
 * and inserts them into a PostgreSQL database.
 */
public final class DBImporter {


    /**
     * Represents the SQL query used for inserting messages into the database.
     */
    private static final String INSERT_QUERY =
        "INSERT INTO s_smart_home.messages (message) VALUES (?)";

    /**
     * Represents the name of the queue used in RabbitMQ for database import.
     */
    private static final String QUEUE_NAME = "DBIMPORT";

    /**
     * Represents the delay (in milliseconds) for retrying operations.
     */
    private static final int DATABASE_RETRY_DELAY = 1000;

    /**
     * Represents the maximum number of attempts for retrying operations.
     * A value of 0 indicates infinite retry attempts.
     */
    private static final int DATABASE_RETRY_MAX_ATTEMPTS = 0; // forever

    /**
     * Counter for tracking the number of received requests.
     */
    private static Counter receivedCounter;

    /**
     * Channel for communication with RabbitMQ.
     */
    private static Channel channel;

    /**
     * Connection to the database.
     */
    private static Connection dbConnection;

    /**
     * Counter name for received messages.
     */
    private static final String COUNTER_RECEIVED_NAME =
        "dbimporter_requests_received_total";
    /**
     * Counter help message for received messages.
     */
    private static final String COUNTER_RECEIVED_HELP =
            "Total Received Messages";

    /**
     * Counter label for received messages.
     */
    private static final String COUNTER_RECEIVED_LABEL =
        "requests_received";

    /**
     * slf4j logger.
     */
    private static final Logger LOG =
        LoggerFactory.getLogger(DBImporter.class);

    private DBImporter() {
    };

    /**
     * Main method to start the DBImporter.
     *
     * @param args Command line arguments (not used)
     * @throws InterruptedException if a thread is interrupted
     * @throws TimeoutException     if a timeout occurs
     * @throws SQLException         if a SQL error occurs
     * @throws IOException          if an I/O error occurs
     */
    public static void main(final String[] args)
        throws InterruptedException,
               TimeoutException,
               SQLException,
               IOException {

        LOG.info("Starting DBImporter.");
        setupMetricServer();
        SharedUtils.startMetricsServer();
        channel = SharedUtils.setupRabbitMQConnection();
        setupDBConnection();
        setupQueue();
        consumeQueue();
    }

    /**
     * Sets up the Prometheus Metric Server.
     */
    private static void setupMetricServer() {
        // Initialize out-of-the-box JVM metrics
        receivedCounter = Counter.builder().name(COUNTER_RECEIVED_NAME)
                .help(COUNTER_RECEIVED_HELP)
                .labelNames(COUNTER_RECEIVED_LABEL)
                .register();
        JvmMetrics.builder().register();
    }

    /**
     * Consumes messages from RabbitMQ.
     *
     * @throws IOException if an I/O error occurs
     */
    private static void consumeQueue() throws IOException {
        // Callback for processing incoming messages
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            LOG.info("Received Message:" + message);
            receivedCounter.labelValues(COUNTER_RECEIVED_LABEL).inc();
            try (
                PreparedStatement preparedStatement =
                    dbConnection.prepareStatement(INSERT_QUERY)) {
                    preparedStatement.setString(1, message);
                    preparedStatement.executeUpdate();
                    LOG.info("Inserted record into the database: " + message);
                    SharedUtils.getEnvVar("HOSTNAME");
            } catch (SQLException e) {
                setupDBConnection();
                e.printStackTrace();
            }
        };

        LOG.info("Starting to consume" + QUEUE_NAME);
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });
    }

    /**
     * Sets up the database connection.
     */
    @SuppressWarnings("all")
    private static void setupDBConnection() {
        for (int attempt = 1; DATABASE_RETRY_MAX_ATTEMPTS == 0
         || attempt <= DATABASE_RETRY_MAX_ATTEMPTS; attempt++) {
            try {
                // Close previous connection if exists
                if (dbConnection != null) {
                    dbConnection.close();
                }
                // Retrieve database connection parameters from environment variables
                String dbHost = SharedUtils.getEnvVar("DB_HOST");
                String dbPort = SharedUtils.getEnvVar("DB_PORT");
                String dbName = SharedUtils.getEnvVar("DB_NAME");
                String dbUser = SharedUtils.getEnvVar("DB_USER");
                String dbPassword = SharedUtils.getEnvVar("DB_PASSWORD");

                // Construct JDBC connection string and establish connection
                String connectionString = "jdbc:postgresql://" + dbHost + ":"
                    + dbPort + "/" + dbName;
                dbConnection = DriverManager.getConnection(connectionString, dbUser, dbPassword);
                break;
            } catch (SQLException e) {
                LOG.info("Failed to connect to database on attempt #" + attempt + ". Retrying...");
                if (DATABASE_RETRY_MAX_ATTEMPTS != 0 && attempt == DATABASE_RETRY_MAX_ATTEMPTS) {
                    throw new RuntimeException(
                        "Failed to connect to database after multiple attempts.", e);
                }
                try {
                    Thread.sleep(DATABASE_RETRY_DELAY);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static void setupQueue() throws IOException {
        // Declare queue, bind to exchange, and start consuming messages
        channel.exchangeDeclare(SharedUtils.getExchangeName(), SharedUtils.getExchangeType());
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(QUEUE_NAME, SharedUtils.getExchangeName(), "");
    }
}
