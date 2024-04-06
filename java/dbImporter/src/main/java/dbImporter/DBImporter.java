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

public class DBImporter {
    private static final String EXCHANGE = "messages";
    private static final String EXCHANGE_TYPE = "fanout";
    private static final String INSERT_QUERY = "INSERT INTO s_smart_home.messages (message) VALUES (?)";
    private static final String QUEUE_NAME = "DBIMPORT";
    private static final int RETRY_DELAY_MILLIS = 1000;
    private static final int RETRY_MAX_ATTEMPTS = 0; //forever
    private static final int METRICS_SERVER_PORT= 8080;
    private static Counter receivedCounter;

    public static void main(String[] args) throws InterruptedException, TimeoutException, SQLException, IOException {
        System.out.printf("Starting DBImporter.%n");
        setupMetricServer();
        consumeQueue();
    }

    @SuppressWarnings("unused")
    private static void setupMetricServer(){
        JvmMetrics.builder().register(); // initialize the out-of-the-box JVM metrics
        receivedCounter = Counter.builder().name("dbimporter_requests_received_total")
            .help("Total number of received requests")
            .labelNames("requests_received")
            .register();
        receivedCounter.labelValues("requests_received").inc();

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

    private static void consumeQueue() {
        try (
            Channel channel = setupRabbitMQConnection();
            Connection dbConnection = setupDBConnection()) {
            channel.exchangeDeclare(EXCHANGE, EXCHANGE_TYPE);
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE, "");

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
                    e.printStackTrace();
                }
            };

            System.out.printf("Starting to consume %s%n", QUEUE_NAME);
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});

        } catch (Exception e) {
            throw new RuntimeException("Failed to consume messages from RabbitMQ queue", e);
        }
    }
        @SuppressWarnings("all")
        private static Channel setupRabbitMQConnection() {
        for (int attempt = 1; RETRY_MAX_ATTEMPTS == 0 || attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(retrieveEnvVariable("RABBITMQ_HOST"));
                factory.setPort(Integer.parseInt(retrieveEnvVariable("RABBITMQ_PORT")));
                factory.setUsername(retrieveEnvVariable("RABBITMQ_USER"));
                factory.setPassword(retrieveEnvVariable("RABBITMQ_PASS"));
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

    @SuppressWarnings("all")
    private static Connection setupDBConnection() {
      
        for (int attempt = 1; RETRY_MAX_ATTEMPTS == 0 || attempt <= RETRY_MAX_ATTEMPTS; attempt++) {
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
        return null;
    }

    protected static String retrieveEnvVariable(String variableName) {
        String variableValue = System.getenv(variableName);
        if (variableValue == null) {
            throw new IllegalArgumentException(
                    "Environment variable " + variableName + " not found. Please set in system environment");
        }
        return variableValue;
    }
}
