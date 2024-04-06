package lightBulbMonitor;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.rabbitmq.client.Delivery;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class LightBulbMonitorTest {

    @Mock
    private Channel channel;

    private LightBulbMonitor lightBulbMonitor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        lightBulbMonitor = new LightBulbMonitor();
        lightBulbMonitor.setChannel(channel);
        lightBulbMonitor.setHostname("test-host");
    }

    @Test
    public void testCreateTriggeredMessage() {
        String hostname = "test-host";
        JSONObject message = lightBulbMonitor.createTriggeredMessage(hostname);

        // Verify the message contents
        assertEquals(hostname, message.getString("hostname"));
        assertEquals("triggered", message.getString("bulb_state"));
        assertNotNull(message.getLong("sent_timestamp"));
        assertEquals(hostname, message.getString("target"));
    }
/*
TODO: Fix this test
    @Test
public void testConsumeQueue() throws IOException {
    // Mock the channel behavior
    doAnswer(invocation -> {
        DeliverCallback deliverCallback = (DeliverCallback) invocation.getArgument(2);
        // Add explicit type casting for CancelCallback if necessary
        String message = "{\"hostname\": \"test-host\", \"bulb_state\": \"on\", \"time_turned_on\": " + (System.currentTimeMillis() - 30000) + "}";
        Delivery delivery = new Delivery(null, null, message.getBytes());
        deliverCallback.handle("", delivery);
        return null;
    }).when(channel).basicConsume(anyString(), anyBoolean(), any(DeliverCallback.class), any(CancelCallback.class));

    // Call the method under test
    lightBulbMonitor.consumeQueue();

    // Verify that the message is processed and sent
    verify(channel, times(8)).basicPublish(anyString(), anyString(), any(), any(byte[].class));
}


    @Test
    public void testSendMessage() throws IOException, InterruptedException {
        JSONObject message = new JSONObject("{\"hostname\": \"test-host\", \"bulb_state\": \"on\", \"time_turned_on\": " + System.currentTimeMillis() + "}");
        lightBulbMonitor.sendMessage(message);

        // Verify that the message is sent through the channel
        verify(channel, times(1)).basicPublish(anyString(), anyString(), any(), any(byte[].class));
    }
*/
    // Add more test cases as needed
}
