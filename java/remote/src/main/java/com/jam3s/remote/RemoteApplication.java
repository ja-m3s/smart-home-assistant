package com.jam3s.remote;

import com.jam3s.sharedutils.SharedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.json.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

@SpringBootApplication
public final class RemoteApplication implements CommandLineRunner {
    /**
     * Stores light bulb data.
     */
    private static final HashMap<String, JSONObject> LIGHT_BULB_HASH_MAP = new HashMap<>();
    /**
     * Represents the name of the queue used for monitoring light bulbs.
     */
    private static final String QUEUE_NAME = "REMOTE";

    /**
     * Regular expression pattern for matching hostnames of light bulbs.
     */
    private static final String LIGHT_BULB_HOSTNAME_REGEX = "light-bulb-\\d+";

    /**
     * Counter name for sent messages.
     */
    private static final String COUNTER_SENT_NAME = "remote_requests_sent_total";

    /**
     * Counter help message for sent messages.
     */
    private static final String COUNTER_SENT_HELP = "Total Sent Messages";

    /**
     * Counter label for received messages.
     */
    private static final String COUNTER_SENT_LABEL = "requests_sent";

    /**
     * Counter name for received messages.
     */
    private static final String COUNTER_RECEIVED_NAME = "remote_requests_received_total";

    /**
     * Counter help message for received messages.
     */
    private static final String COUNTER_RECEIVED_HELP = "Total Received Messages";

    /**
     * Counter label for received messages.
     */
    private static final String COUNTER_RECEIVED_LABEL = "requests_received";

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RemoteApplication.class);

    /**
     * Counter for tracking the number of received messages.
     */
    private static Counter receivedCounter;

    /**
     * Counter for tracking the number of sent messages.
     */
    private static Counter sentCounter;

    /**
     * The hostname of the current environment.
     */
    private static String hostname;

    /**
     * Main Method.
     *
     * @param args
     */
    public static void main(final String[] args) {
        SpringApplication.run(RemoteApplication.class, args);
    }

    /**
     * Consumes messages from the RabbitMQ queue.
     */
    private static void consumeQueue() {
        try {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                LOG.info("Received: {}", message);
                receivedCounter.labelValues(COUNTER_RECEIVED_LABEL).inc();
                // Make json object from message
                JSONObject msg = new JSONObject(message);

                // Check message is from a lightbulb, if not, disregard it.
                String originHostname = msg.getString("hostname");
                LOG.info("Message from: {}", originHostname);
                if (!originHostname.matches(LIGHT_BULB_HOSTNAME_REGEX)) {
                    LOG.info("originHostname is not a light bulb. Disregarding message.");
                    return;
                }
                LOG.info("originHostname is a light bulb. Processing.");

                LIGHT_BULB_HASH_MAP.put(originHostname, msg);
            };

            LOG.info("Starting to consume: " + QUEUE_NAME);
            Channel channel = SharedUtils.getChannel();

            while (true) {
                try {
                    channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
                    });
                    break; // Exit the loop if basicConsume is successful
                } catch (IOException e) {
                    LOG.error("Error occurred while consuming from the queue. Attempting to reconnect to RabbitMQ...");
                    SharedUtils.setupRabbitMQConnection(); // Attempt to set up RabbitMQ connection again
                    channel = SharedUtils.getChannel(); // Get a new channel
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to consume messages from RabbitMQ queue", e);
        }
    }

    /**
     * Sends a message to RabbitMQ.
     *
     * @param message The message to be sent.
     */
    private static void sendMessage(final JSONObject message) {
        Channel channel = SharedUtils.getChannel();
        while (true) {
            try {
                channel.basicPublish(SharedUtils.getExchangeName(), "", null,
                        message.toString().getBytes(StandardCharsets.UTF_8));
                break; // Exit the loop if basicPublish is successful
            } catch (IOException e) {
                LOG.error("Error occurred while publishing to the queue. Attempting to reconnect to RabbitMQ...");
                SharedUtils.setupRabbitMQConnection(); // Attempt to set up RabbitMQ connection again
                channel = SharedUtils.getChannel(); // Get a new channel
            }
        }
        sentCounter.labelValues(COUNTER_SENT_LABEL).inc();
        LOG.info("Sent '{}'", message);
    }

    /**
     * Sets up the metric server.
     */
    private static void setupMetricServer() {
        JvmMetrics.builder().register(); // initialize the out-of-the-box JVM metrics
        receivedCounter = Counter.builder().name(COUNTER_RECEIVED_NAME)
                .help(COUNTER_RECEIVED_HELP)
                .labelNames(COUNTER_RECEIVED_LABEL)
                .register();

        sentCounter = Counter.builder().name(COUNTER_SENT_NAME)
                .help(COUNTER_SENT_HELP)
                .labelNames(COUNTER_SENT_LABEL)
                .register();
    }

    /**
     * Creates a triggered message.
     *
     * @param target The target hostname.
     * @return The JSON message.
     */
    static JSONObject createTurnOnMessage(final String target) {
        JSONObject msg = new JSONObject();
        msg.put("hostname", hostname);
        msg.put("bulb_state", "triggered");
        msg.put("target", target);
        msg.put("sent_timestamp", System.currentTimeMillis());

        LOG.info("JSON message: {}", msg);

        return msg;
    }

    static HashMap<String, JSONObject> getLightBulbStatus() {
        return LIGHT_BULB_HASH_MAP;
    }

    @Override
    public void run(final String... args) {
        SharedUtils.setupRabbitMQConnection();
        hostname = SharedUtils.getEnvVar("HOSTNAME");
        System.out.println(hostname);
        setupMetricServer();
        SharedUtils.startMetricsServer();

        try {
            SharedUtils.setupQueue(QUEUE_NAME);
        } catch (IOException e) {
            LOG.info(e.toString());
        }
        consumeQueue();
    }
}
