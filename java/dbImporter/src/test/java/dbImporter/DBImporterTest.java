package dbImporter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.Basic.Deliver;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBImporterTest {

    @Mock
    private Channel channel;

    @Mock
    private Connection dbConnection;

    @Mock
    private PreparedStatement preparedStatement;

    private DBImporter dbImporter;

    @Before
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        dbImporter = new DBImporter();
        dbImporter.setChannel(channel);
        dbImporter.setDbConnection(dbConnection);
    }

/* TODO: Fix test
    @Test
    public void testConsumeQueue() throws IOException {
    // Mock the channel behavior
    doAnswer(invocation -> {
        DeliverCallback deliverCallback = (DeliverCallback) invocation.getArgument(2);
        String message = "{\"hostname\": \"test-host\", \"bulb_state\": \"on\", \"time_turned_on\": " + (System.currentTimeMillis() - 30000) + "}";
        Delivery delivery = new Delivery(null, null, message.getBytes());
        deliverCallback.handle("", delivery);
        return null;
    }).when(channel).basicConsume(anyString(), anyBoolean(), any(DeliverCallback.class), any());

    // Call the method under test
    try {
        dbImporter.consumeQueue();
    } catch (Exception e) {
        e.printStackTrace();
    }

    // Verify that the message is processed and sent
    // Add your verification code here
}
*/
}
