package dbImporter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class DBImporter {

    private static final String EXCHANGE = "messages";
    private static final String EXCHANGE_TYPE = "fanout";
    static final String INSERT_QUERY = "INSERT INTO s_smart_home.messages (message) VALUES (?)";
    private static final String QUEUE_NAME = "DBIMPORT";
    private static final int RETRY_DELAY_MILLIS = 1000;
    private final Counter requestsReceivedTotal = Counter.build()
    .name("dbimporter_requests_received_total")
    .help("Total number of received requests.")
    .register();

    private Channel channel;
    private Connection dbConnection;

    public DBImporter() {
        this.channel = setupRabbitMQConnection();
        this.dbConnection = setupDBConnection();
    }

    private Channel setupRabbitMQConnection() {
        int maxRetries = getMaxRetries();
        for (int attempt = 1; maxRetries == 0 || attempt <= maxRetries; attempt++) {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(retrieveEnvVariable("RABBITMQ_HOST"));
                factory.setPort(Integer.parseInt(retrieveEnvVariable("RABBITMQ_PORT")));
                factory.setUsername(retrieveEnvVariable("RABBITMQ_USER"));
                factory.setPassword(retrieveEnvVariable("RABBITMQ_PASS"));
                return factory.newConnection().createChannel();
            } catch (Exception e) {
                System.out.printf("Failed to connect to RabbitMQ on attempt #%d. Retrying...%n", attempt);
                if (maxRetries != 0 && attempt == maxRetries) {
                    throw new RuntimeException("Failed to connect to RabbitMQ after multiple attempts.", e);
                }
                try {
                    Thread.sleep(RETRY_DELAY_MILLIS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return null; // Unreachable code, added to satisfy compiler
    }

    private Connection setupDBConnection() {
        int maxRetries = getMaxRetries();
        for (int attempt = 1; maxRetries == 0 || attempt <= maxRetries; attempt++) {
            try {
                String dbHost = retrieveEnvVariable("DB_HOST");
                String dbPort = retrieveEnvVariable("DB_PORT");
                String dbName = retrieveEnvVariable("DB_NAME");
                String dbUser = retrieveEnvVariable("DB_USER");
                String dbPassword = retrieveEnvVariable("DB_PASSWORD");

                String connectionString = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
                return DriverManager.getConnection(connectionString, dbUser, dbPassword);
            } catch (SQLException e) {
                System.out.printf("Failed to connect to database on attempt #%d. Retrying...%n", attempt);
                if (maxRetries != 0 && attempt == maxRetries) {
                    throw new RuntimeException("Failed to connect to database after multiple attempts.", e);
                }
                try {
                    Thread.sleep(RETRY_DELAY_MILLIS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return null; // Unreachable code, added to satisfy compiler
    }

    private int getMaxRetries() {
        String maxRetriesStr = System.getenv("MAX_CONNECTION_RETRIES");
        if (maxRetriesStr != null) {
            try {
                return Integer.parseInt(maxRetriesStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid value for MAX_CONNECTION_RETRIES. Using default value.");
            }
        }
        // Default to infinite retries if MAX_CONNECTION_RETRIES is not set or invalid
        return 0;
    }

    public void consumeQueue() {
        try {
            channel.exchangeDeclare(EXCHANGE, EXCHANGE_TYPE);
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE, "");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.printf("Received Message: %s%n", message);
                requestsReceivedTotal.inc();
                try (PreparedStatement preparedStatement = dbConnection.prepareStatement(INSERT_QUERY)) {
                    preparedStatement.setString(1, message);
                    preparedStatement.executeUpdate();
                    System.out.printf("Inserted record into the database: %s from %s%n", message, retrieveEnvVariable("HOSTNAME"));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            };

            System.out.printf("Starting to consume %s%n", QUEUE_NAME);
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});

        } catch (IOException e) {
            throw new RuntimeException("Failed to consume messages from RabbitMQ queue", e);
        }
    }

    private static String retrieveEnvVariable(String variableName) {
        String variableValue = System.getenv(variableName);
        if (variableValue == null) {
            throw new IllegalArgumentException("Environment variable " + variableName + " not found. Please set in system environment");
        }
        return variableValue;
    }

    public static void main(String[] args) {
        System.out.printf("Starting DBImporter.%n");
        DBImporter importer = new DBImporter();
        try {
            HTTPServer metrics_server = new HTTPServer(8080);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        importer.consumeQueue();
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setDbConnection(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }
}
