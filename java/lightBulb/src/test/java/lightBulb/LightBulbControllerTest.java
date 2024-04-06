package lightBulb;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;

public class LightBulbControllerTest {

    @Mock
    private Channel channel;

    private LightBulbController lightBulbController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        lightBulbController = new LightBulbController();
        lightBulbController.setChannel(channel);
        lightBulbController.setHostname("test-host");
    }

    /*@Test
    public void testCreateMessage() {
        JSONObject message = lightBulbController.createMessage();

        // Verify the message contents
        assertEquals(lightBulbController.getHostname(), message.getString("hostname"));
        assertEquals(LightBulb.LightBulbState.ON.getValue(), message.getString("bulb_state"));
        assertNotNull(message.getLong("sent_timestamp"));
        assertNotNull(message.getLong("time_turned_on"));
    }
    
    @Test
    public void testSendMessage() throws IOException {
        JSONObject message = lightBulbController.createMessage();
        lightBulbController.sendMessage(message);

        // Verify that the message is sent through the channel
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<AMQP.BasicProperties> propertiesCaptor = ArgumentCaptor.forClass(AMQP.BasicProperties.class);
        ArgumentCaptor<byte[]> messageCaptor = ArgumentCaptor.forClass(byte[].class);
        
        verify(channel, times(1)).exchangeDeclare(anyString(), anyString());
        verify(channel, times(1)).queueBind(anyString(), anyString(), anyString());
        verify(channel, times(1)).basicPublish(
            exchangeCaptor.capture(),
            routingKeyCaptor.capture(),
            propertiesCaptor.capture(),
            messageCaptor.capture()
        );
    }
 */
    // Add more test cases as needed
}
