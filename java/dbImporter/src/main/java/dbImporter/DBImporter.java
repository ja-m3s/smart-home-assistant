package dbImporter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class DBImporter {

    private final static String EXCHANGE = "messages";
    private static final String EXCHANGE_TYPE = "fanout";
    private static final String INSERT_QUERY = "INSERT INTO s_smart_home.messages (message) VALUES (?)";

    private ConnectionFactory connectionFactory;
    private com.rabbitmq.client.Connection connection;
    private Channel channel;
    private String hostname;
    private String queue_name = "DBIMPORT";
    private Connection dbConnection;

    public DBImporter(String rabbitmqHost, String rabbitmqPort, String rabbitmqUser, String rabbitmqPass, String hostname, String dbHost, String dbPort, String dbName, String dbUser, String dbPassword) {
        this.connectionFactory = createConnectionFactory(rabbitmqHost, rabbitmqPort, rabbitmqUser, rabbitmqPass);
        this.hostname = hostname;
        this.dbConnection = setupDBConnection(dbHost, dbPort, dbName, dbUser, dbPassword, null); // Retry up to 20 times for database connection
        setupRabbitMQConnection();

    }

    private ConnectionFactory createConnectionFactory(String rabbitmqHost, String rabbitmqPort, String rabbitmqUser, String rabbitmqPass) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitmqHost);
        factory.setPort(Integer.parseInt(rabbitmqPort));
        factory.setUsername(rabbitmqUser);
        factory.setPassword(rabbitmqPass);
        return factory;
    }

    private void setupRabbitMQConnection() {
        connectToRabbitMQ(20); // Retry up to 20 times
        try {
            setupExchange();
            setupQueue();
            consumeQueue();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void setupExchange() throws IOException {
        this.channel.exchangeDeclare(EXCHANGE, EXCHANGE_TYPE);
    }

    private void connectToRabbitMQ(int maxAttempts) {
        int attempts = 0;
        boolean connected = false;

        while (!connected && (maxAttempts == 0 || attempts < maxAttempts)) {
            try {
                this.connection = connectionFactory.newConnection();
                this.channel = connection.createChannel();
                connected = true;
                System.out.println("Connected to RabbitMQ");
            } catch (Exception e) {
                attempts++;
                System.out.println("Connection attempt #" + attempts + " failed. Retrying...");
                try {
                    Thread.sleep(1000); // Wait for 1 second before retrying
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!connected) {
            System.out.println("Failed to connect to RabbitMQ after " + attempts + " attempts.");
            System.exit(1);
        }
    }

    private void setupQueue() throws IOException {
        String queueName = channel.queueDeclare(queue_name, false, false, false, null).getQueue();
        channel.queueBind(queueName, EXCHANGE, "");
        System.out.println("Created queue: " + queueName);
    }

    private static String retrieveEnvVariable(String variableName) {
        String variableValue = System.getenv(variableName);
        if (variableValue == null) {
            System.out.println("Environment variable " + variableName + " not found. Please set in system environment");
            System.exit(1);
        } else {
            System.out.println("Value of " + variableName + ": " + variableValue);
        }
        return variableValue;
    }

    private void consumeQueue() {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Received '" + message + "'");

            // Perform database insert
            try (PreparedStatement preparedStatement = dbConnection.prepareStatement(INSERT_QUERY)) {
                preparedStatement.setString(1, message);
                preparedStatement.executeUpdate();
                System.out.println("Inserted record from " + hostname + " into the database.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };

        try {
            System.out.println("Starting to consume " + this.queue_name);
            this.channel.basicConsume(this.queue_name, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection setupDBConnection(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword, Integer maxRetries) {
        int retryDelay = 2000; // milliseconds
        int retryCount = 0;

        System.out.println("dbhost: " + dbHost + ":" + dbPort + "/" + dbName);
        System.out.println("db credentials: " + dbUser + "/" + dbPassword);

        while (maxRetries == null || retryCount < maxRetries) {
            try {
                // Establish connection to the database
                Connection connection = DriverManager.getConnection(
                        "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName,
                        dbUser,
                        dbPassword
                );
                System.out.println("Successfully established connection to database");
                return connection;
            } catch (SQLException e) {
                System.out.println("Failed to connect to Database: " + e.getMessage());
                if (maxRetries != null) {
                    System.out.println("Retry " + (retryCount + 1) + " of " + maxRetries + "...");
                } else {
                    System.out.println("Retrying in " + retryDelay / 1000 + " seconds...");
                }
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                retryCount++;
            }
        }

        System.out.println("Failed to connect after retries.");
        return null;
    }

    /**
     * MAIN
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Starting DBImporter.");

        String rabbitmqHost = retrieveEnvVariable("RABBITMQ_HOST");
        String rabbitmqPort = retrieveEnvVariable("RABBITMQ_PORT");
        String rabbitmqUser = retrieveEnvVariable("RABBITMQ_USER");
        String rabbitmqPass = retrieveEnvVariable("RABBITMQ_PASS");
        String hostname = retrieveEnvVariable("HOSTNAME");
        String dbHost = retrieveEnvVariable("DB_HOST");
        String dbPort = retrieveEnvVariable("DB_PORT");
        String dbName = retrieveEnvVariable("DB_NAME");
        String dbUser = retrieveEnvVariable("DB_USER");
        String dbPassword = retrieveEnvVariable("DB_PASSWORD");

        DBImporter controller = new DBImporter(rabbitmqHost, rabbitmqPort, rabbitmqUser, rabbitmqPass, hostname, dbHost, dbPort, dbName, dbUser, dbPassword);
    }
}
