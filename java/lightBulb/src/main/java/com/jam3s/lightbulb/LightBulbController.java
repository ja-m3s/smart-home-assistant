package com.jam3s.lightbulb;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jam3s.lightbulb.LightBulb.LightBulbState;
import com.jam3s.sharedutils.SharedUtils;
import com.rabbitmq.client.AMQP;

/**
 * The LightBulbController class manages the communication with RabbitMQ
 * and controls the state of the light bulb.
 */
public final class LightBulbController {

    /**
     * Represents the name of the queue used in messaging.
     * If it's "" it broadcasts to all queues on the exchange.
     */
    private static final String QUEUE_NAME = "";

    /**
     * Represents the time interval (in milliseconds) for sending messages.
     */
    private static final Integer SEND_MESSAGE_POLL_TIME = 5000; // 5 seconds

    /**
     * Regular expression pattern for matching hostnames of light bulb monitors.
     */
    private static final String LIGHT_BULB_MONITOR_HOSTNAME_REGEX = "light-bulb-monitor-.+";

    /**
     * Represents the current state of the light bulb.
     */
    private static final LightBulb LIGHT_BULB = new LightBulb();;

    /**
     * The hostname of the current environment.
     */
    private static String hostname;

    /**
     * Counter for tracking the number of received messages.
     */
    private static Counter receivedCounter;

    /**
     * Counter for tracking the number of sent messages.
     */
    private static Counter sentCounter;

    /**
     * Counter name for sent messages.
     */
    private static final String COUNTER_SENT_NAME = "lightbulb_requests_sent_total";

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
    private static final String COUNTER_RECEIVED_NAME = "lightbulb_requests_received_total";

    /**
     * Counter help message for received messages.
     */
    private static final String COUNTER_RECEIVED_HELP = "Total Received Messages";

    /**
     * Counter label for received messages.
     */
    private static final String COUNTER_RECEIVED_LABEL = "requests_received";

    /**
     * slf4j logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LightBulbController.class);

    /**
     * Regex for detecting a remote.
     */
    protected static final String REMOTE_HOSTNAME_REGEX = "remote-.+";

    private LightBulbController() {
    };

    /**
     * The main method.
     *
     * @param args The command-line arguments.
     * @throws IOException          if an I/O error occurs.
     * @throws InterruptedException if the thread is interrupted.
     */
    public static void main(final String[] args) throws IOException, InterruptedException {
        LOG.info("Starting Light Bulb.");
        LOG.info(LIGHT_BULB.toString());
        hostname = SharedUtils.getEnvVar("HOSTNAME");
        SharedUtils.setupRabbitMQConnection();
        SharedUtils.setupQueue(QUEUE_NAME);
        setupMetricServer();
        SharedUtils.startMetricsServer();
        receiveMessage();
        while (true) {
            sendMessage(createMessage());
            Thread.sleep(SEND_MESSAGE_POLL_TIME); // Sleep for 5 seconds
        }
    }

    private static void setupMetricServer() {
        JvmMetrics.builder().register(); // initialize the out-of-the-box JVM metrics
        sentCounter = Counter.builder().name(COUNTER_SENT_NAME)
                .help(COUNTER_SENT_HELP)
                .labelNames(COUNTER_SENT_LABEL)
                .register();

        receivedCounter = Counter.builder().name(COUNTER_RECEIVED_NAME)
                .help(COUNTER_RECEIVED_HELP)
                .labelNames(COUNTER_RECEIVED_LABEL)
                .register();
    }

    /**
     * Sends a message to RabbitMQ.
     *
     * @param message The message to be sent.
     * @throws IOException if an I/O error occurs.
     */
    private static void sendMessage(final JSONObject message) throws IOException {
        // Only broadcast when lightbulb is on
        Channel channel = SharedUtils.getChannel();

        while (true) {
            try {
                channel.basicPublish(SharedUtils.getExchangeName(), QUEUE_NAME, null,
                message.toString().getBytes(StandardCharsets.UTF_8));
                break; // Exit the loop if basicPublish is successful
            } catch (IOException e) {
                LOG.error("Error occurred while publishing from the queue. Attempting to reconnect to RabbitMQ...");
                SharedUtils.setupRabbitMQConnection(); // Attempt to set up RabbitMQ connection again
                channel = SharedUtils.getChannel(); // Get a new channel
            }
        }
        sentCounter.labelValues(COUNTER_SENT_LABEL).inc();
        LOG.info("Sent :{}", message);

    }

    /**
     * Creates a JSON message containing information about the light bulb state.
     *
     * @return The JSON message.
     */
    private static JSONObject createMessage() {
        JSONObject msg = new JSONObject();
        msg.put("hostname", hostname);
        msg.put("bulb_state", LIGHT_BULB.getState());
        msg.put("sent_timestamp", System.currentTimeMillis());
        msg.put("time_turned_on", LIGHT_BULB.getTimeTurnedOn());

        LOG.info("JSON message: " + msg);

        return msg;
    }

    /**
     * Receives a message from RabbitMQ.
     *
     * @throws IOException if an I/O error occurs.
     */
    private static void receiveMessage() throws IOException {
        Channel channel = SharedUtils.getChannel();

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(final String consumerTag,
                                       final Envelope envelope,
                                       final AMQP.BasicProperties properties,
                                       final byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                LOG.info("Received message: " + message);
                receivedCounter.labelValues(COUNTER_RECEIVED_LABEL).inc();
                // Make json object from message
                JSONObject msg = new JSONObject(message);

                // Check message is from a lightbulb, if not, disregard it.
                String originHostname = msg.getString("hostname");
                LOG.info("Message from: %s%n", originHostname);
                if (!originHostname.matches(LIGHT_BULB_MONITOR_HOSTNAME_REGEX)
                        && !originHostname.matches(REMOTE_HOSTNAME_REGEX)) {
                    LOG.info("originHostname is not a light bulb monitor. Disregarding message.");
                    return;
                }
                LOG.info("originHostname is a light bulb monitor or remote. Processing.");
                String target = msg.getString("target");

                // is message for this light?
                if (!target.equals(hostname)) {
                  return;
                }

                LOG.info("Toggling light");
                if (LIGHT_BULB.getState() == LightBulbState.ON) {
                    LIGHT_BULB.setState(LightBulbState.OFF);
                } else {
                    LIGHT_BULB.setState(LightBulbState.ON);
                    LIGHT_BULB.setTimeTurnedOn(System.currentTimeMillis());
                }
            }
        };

        LOG.info("Waiting for messages. To exit press Ctrl+C");

        while (true) {
            try {
                channel.basicConsume(QUEUE_NAME, true, consumer);
                break; // Exit the loop if basicConsume is successful
            } catch (IOException e) {
                LOG.error("Error occurred while consuming from the queue. Attempting to reconnect to RabbitMQ...");
                SharedUtils.setupRabbitMQConnection(); // Attempt to set up RabbitMQ connection again
                channel = SharedUtils.getChannel(); // Get a new channel
            }
        }
    }

}
