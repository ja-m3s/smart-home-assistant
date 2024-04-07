package dbImporter;

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
public class DBImporter {

/**
 * Represents the name of the exchange used in RabbitMQ.
 */
private static final String EXCHANGE = "messages";

/**
 * Represents the type of exchange used in RabbitMQ.
 */
private static final String EXCHANGE_TYPE = "fanout";

/**
 * Represents the SQL query used for inserting messages into the database.
 */
private static final String INSERT_QUERY = "INSERT INTO s_smart_home.messages (message) VALUES (?)";

/**
 * Represents the name of the queue used in RabbitMQ for database import.
 */
private static final String QUEUE_NAME = "DBIMPORT";

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
 * Connection to the database.
 */
private static Connection dbConnection;


    /**
     * Main method to start the DBImporter.
     * @param args Command line arguments (not used)
     * @throws InterruptedException if a thread is interrupted
     * @throws TimeoutException if a timeout occurs
     * @throws SQLException if a SQL error occurs
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws InterruptedException, TimeoutException, SQLException, IOException {
        System.out.printf("Starting DBImporter.%n");
        setupMetricServer();
        setupRabbitMQConnection();
        setupDBConnection();
        consumeQueue();
    }

    /**
     * Retrieves an environment variable.
     * @param variableName The name of the environment variable
     * @return The value of the environment variable
     * @throws IllegalArgumentException if the environment variable is not found
     */
    protected static String retrieveEnvVariable(String variableName) {
        String variableValue = System.getenv(variableName);
        if (variableValue == null) {
            throw new IllegalArgumentException(
                    "Environment variable " + variableName + " not found. Please set in system environment");
        }
        return variableValue;
    }

    /**
     * Sets up the Prometheus Metric Server.
     */
    @SuppressWarnings("unused")
    private static void setupMetricServer() {
        // Initialize out-of-the-box JVM metrics
        JvmMetrics.builder().register();
        receivedCounter = Counter.builder().name("dbimporter_requests_received_total")
                .help("Total number of received requests")
                .labelNames("requests_received")
                .register();
        receivedCounter.labelValues("requests_received").inc();

        // Start HTTP server for Prometheus metrics
        Thread serverThread = new Thread(() -> {
            try {
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
     * Consumes messages from RabbitMQ.
     * @throws IOException if an I/O error occurs
     */
    private static void consumeQueue() throws IOException {
        // Callback for processing incoming messages
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.printf("Received Message: %s%n", message);
            receivedCounter.labelValues("requests_received").inc();
            try (PreparedStatement preparedStatement = dbConnection.prepareStatement(INSERT_QUERY)) {
                preparedStatement.setString(1, message);
                preparedStatement.executeUpdate();
                System.out.printf("Inserted record into the database: %s from %s%n", message,
                        retrieveEnvVariable("HOSTNAME"));
            } catch (SQLException e) {
                setupDBConnection();
                e.printStackTrace();
            }
        };

        // Declare queue, bind to exchange, and start consuming messages
        channel.exchangeDeclare(EXCHANGE, EXCHANGE_TYPE);
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE, "");
        System.out.printf("Starting to consume %s%n", QUEUE_NAME);
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
    }

    /**
     * Sets up the RabbitMQ connection.
     */
    @SuppressWarnings("all")
    private static void setupRabbitMQConnection() {
        for (int attempt = 1; RETRY_MAX_ATTEMPTS == 0 || attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(retrieveEnvVariable("RABBITMQ_HOST"));
                factory.setPort(Integer.parseInt(retrieveEnvVariable("RABBITMQ_PORT")));
                factory.setUsername(retrieveEnvVariable("RABBITMQ_USER"));
                factory.setPassword(retrieveEnvVariable("RABBITMQ_PASS"));
                channel = factory.newConnection().createChannel();
                break;
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
    }

    /**
     * Sets up the database connection.
     */
    @SuppressWarnings("all")
    private static void setupDBConnection() {
        for (int attempt = 1; RETRY_MAX_ATTEMPTS == 0 || attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
            try {
                // Close previous connection if exists
                if (dbConnection != null) {
                    dbConnection.close();
                }
                // Retrieve database connection parameters from environment variables
                String dbHost = retrieveEnvVariable("DB_HOST");
                String dbPort = retrieveEnvVariable("DB_PORT");
                String dbName = retrieveEnvVariable("DB_NAME");
                String dbUser = retrieveEnvVariable("DB_USER");
                String dbPassword = retrieveEnvVariable("DB_PASSWORD");

                // Construct JDBC connection string and establish connection
                String connectionString = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
                dbConnection = DriverManager.getConnection(connectionString, dbUser, dbPassword);
                break;
            } catch (SQLException e) {
                System.out.printf("Failed to connect to database on attempt #%d. Retrying...%n", attempt);
                if (RETRY_MAX_ATTEMPTS != 0 && attempt == RETRY_MAX_ATTEMPTS) {
                    throw new RuntimeException("Failed to connect to database after multiple attempts.", e);
                }
                try {
                    Thread.sleep(RETRY_DELAY_MILLIS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
