package com.jam3s.lightbulbmonitor;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import com.jam3s.sharedutils.SharedUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * The LightBulbMonitor class monitors the state of light bulbs and sends
 * triggered messages accordingly.
 */
public final class LightBulbMonitor {

    /**
     * Represents the name of the queue used for monitoring light bulbs.
     */
    protected static final String QUEUE_NAME = "LIGHTBULBMONITOR";

    /**
     * Regular expression pattern for matching hostnames of light bulbs.
     */
    protected static final String LIGHT_BULB_HOSTNAME_REGEX = "light-bulb-\\d+";

    /**
     * Counter name for sent messages.
     */
    protected static final String COUNTER_SENT_NAME = "lightbulbmonitor_requests_sent_total";

    /**
     * Counter help message for sent messages.
     */
    protected static final String COUNTER_SENT_HELP = "Total Sent Messages";

    /**
     * Counter label for received messages.
     */
    protected static final String COUNTER_SENT_LABEL = "requests_sent";

    /**
     * Counter name for received messages.
     */
    protected static final String COUNTER_RECEIVED_NAME = "lightbulbmonitor_requests_received_total";

    /**
     * Counter help message for received messages.
     */
    protected static final String COUNTER_RECEIVED_HELP = "Total Received Messages";

    /**
     * Counter label for received messages.
     */
    protected static final String COUNTER_RECEIVED_LABEL = "requests_received";

    /**
     * slf4j logger.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(LightBulbMonitor.class);

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
     * Represents the time limit (in milliseconds) for the light bulb to remain on.
     */
    private static long lightTimeout;

    private LightBulbMonitor() {
    };

    /**
     * The main method.
     *
     * @param args The command-line arguments.
     * @throws InterruptedException if the thread is interrupted.
     * @throws IOException          if an I/O error occurs.
     */
    public static void main(final String[] args) throws InterruptedException, IOException {
        LOG.info("Starting LightBulbMonitor.");
        lightTimeout = Long.parseLong(SharedUtils.getEnvVar("LIGHT_ON_LIMIT"));
        hostname = SharedUtils.getEnvVar("HOSTNAME");
        SharedUtils.setupRabbitMQConnection();
        setupMetricServer();
        SharedUtils.startMetricsServer();
        SharedUtils.setupQueue(QUEUE_NAME);
        consumeQueue();
    }

    /**
     * Sets up the metric server.
     */
    protected static void setupMetricServer() {
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
     * Consumes messages from the RabbitMQ queue.
     */
    protected static void consumeQueue() {
        try {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                LOG.info("Received: " + message);
                receivedCounter.labelValues(COUNTER_RECEIVED_LABEL).inc();
                // Make json object from message
                JSONObject msg = new JSONObject(message);

                // Check message is from a lightbulb, if not, disregard it.
                String originHostname = msg.getString("hostname");
                LOG.info("Message from: " + originHostname);
                if (!originHostname.matches(LIGHT_BULB_HOSTNAME_REGEX)) {
                    LOG.info("originHostname is not a light bulb. Disregarding message.");
                    return;
                }
                LOG.info("originHostname is a light bulb. Processing.");

                // Parse out the fields we need
                String bulbState = msg.getString("bulb_state");
                long sentTimestamp = msg.getLong("time_turned_on");
                long currentTimestamp = System.currentTimeMillis();

                if (bulbState.equals("ON") && sentTimestamp + lightTimeout <= currentTimestamp) {
                    // Timestamp is 20 seconds or more in the past
                    LOG.info("Switching off light");
                    JSONObject triggerMsg = createTriggeredMessage(originHostname);
                    try {
                        sendMessage(triggerMsg);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    LOG.info("Not switching off light");
                }
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
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the thread is interrupted.
     */
    protected static void sendMessage(final JSONObject message) throws IOException, InterruptedException {
        Channel channel = SharedUtils.getChannel();
        while (true) {
            try {
                channel.basicPublish(SharedUtils.getExchangeName(), "", null,
                message.toString().getBytes(StandardCharsets.UTF_8));           
                break; // Exit the loop if basicPublish is successful
            } catch (IOException e) {
                LOG.error("Error occurred while consuming from the queue. Attempting to reconnect to RabbitMQ...");
                SharedUtils.setupRabbitMQConnection(); // Attempt to set up RabbitMQ connection again
                channel = SharedUtils.getChannel(); // Get a new channel
            }
        } 
        sentCounter.labelValues(COUNTER_SENT_LABEL).inc();
        LOG.info("Sent '" + message + "'");
    }

    /**
     * Creates a triggered message.
     *
     * @param target The target hostname.
     * @return The JSON message.
     */
    protected static JSONObject createTriggeredMessage(final String target) {
        JSONObject msg = new JSONObject();
        msg.put("hostname", hostname);
        msg.put("bulb_state", "triggered");
        msg.put("target", target);
        msg.put("sent_timestamp", System.currentTimeMillis());

        LOG.info("JSON message: " + msg);

        return msg;
    }
}
