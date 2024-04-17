package com.jam3s.lightbulbmonitor;

import com.jam3s.sharedutils.SharedUtils;
import com.rabbitmq.client.Channel;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.io.IOException;

public class LightBulbMonitorTest {

    /**
     * Mocked channel.
     */
    @Mock
    private Channel channelMock;

    /**
     * Test setup.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SharedUtils.setChannel(channelMock);
    }

    /**
     * Test sendMessage.
     * @throws IOException
     * @throws InterruptedException
     */
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
