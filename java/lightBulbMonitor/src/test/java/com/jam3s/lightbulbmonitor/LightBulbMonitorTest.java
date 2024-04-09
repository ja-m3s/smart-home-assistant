package com.jam3s.lightbulbmonitor;

import com.jam3s.sharedutils.SharedUtils;
import com.rabbitmq.client.Channel;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class LightBulbMonitorTest {

    @Mock
    private Channel channelMock;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SharedUtils.setChannel(channelMock);
    }

    @Test
    public void testSendMessage() throws IOException, InterruptedException {
        // Arrange
        JSONObject message = new JSONObject()
                .put("hostname", "light-bulb-1")
                .put("bulb_state", "triggered")
                .put("target", "test-target")
                .put("sent_timestamp", System.currentTimeMillis());

        // Act
        LightBulbMonitor.setupMetricServer();
        LightBulbMonitor.sendMessage(message);

        // Assert
        // Verify that the message was sent
        verify(channelMock).basicPublish(anyString(), eq(""), eq(null), any(byte[].class));
    }
}