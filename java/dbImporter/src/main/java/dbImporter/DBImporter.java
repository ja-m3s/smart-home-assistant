package dbImporter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/**
 * The DBImporter class is responsible for importing messages from a RabbitMQ queue
 * and storing them into a database.
 */
public class DBImporter {

    private static final String EXCHANGE = "messages";
    private static final String EXCHANGE_TYPE = "fanout";
    private static final String INSERT_QUERY = "INSERT INTO s_smart_home.messages (message) VALUES (?)";
    private static final String QUEUE_NAME = "DBIMPORT";

    private final Channel channel;
    private final Connection dbConnection;

    /**
     * Constructor for DBImporter class. Initializes RabbitMQ and database connections.
     */
    public DBImporter() {
        this.channel = setupRabbitMQConnection();
        this.dbConnection = setupDBConnection();
    }

    /**
     * Sets up the RabbitMQ connection.
     */
    private Channel setupRabbitMQConnection() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(retrieveEnvVariable("RABBITMQ_HOST"));
            factory.setPort(Integer.parseInt(retrieveEnvVariable("RABBITMQ_PORT")));
            factory.setUsername(retrieveEnvVariable("RABBITMQ_USER"));
            factory.setPassword(retrieveEnvVariable("RABBITMQ_PASS"));
            return factory.newConnection().createChannel();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up RabbitMQ connection", e);
        }
    }

    /**
     * Sets up the database connection.
     */
    private Connection setupDBConnection() {
        try {
            String dbHost = retrieveEnvVariable("DB_HOST");
            String dbPort = retrieveEnvVariable("DB_PORT");
            String dbName = retrieveEnvVariable("DB_NAME");
            String dbUser = retrieveEnvVariable("DB_USER");
            String dbPassword = retrieveEnvVariable("DB_PASSWORD");

            String connectionString = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
            return DriverManager.getConnection(connectionString, dbUser, dbPassword);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set up database connection", e);
        }
    }

    /**
     * Consumes messages from the RabbitMQ queue and inserts them into the database.
     */
    public void consumeQueue() {
        try {
            channel.exchangeDeclare(DBImporter.EXCHANGE, DBImporter.EXCHANGE_TYPE);
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE, "");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.printf("Received Message: %s%n", message);

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

    /**
     * Retrieves an environment variable.
     */
    private static String retrieveEnvVariable(String variableName) {
        String variableValue = System.getenv(variableName);
        if (variableValue == null) {
            throw new IllegalArgumentException("Environment variable " + variableName + " not found. Please set in system environment");
        }
        return variableValue;
    }

    /**
     * The main method. It starts the DBImporter.
     */
    public static void main(String[] args) {
        System.out.printf("Starting DBImporter.%n");
        DBImporter importer = new DBImporter();
        importer.consumeQueue();
    }
}
