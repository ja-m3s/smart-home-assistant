package com.jam3s.sharedutils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;


/**
 * Unit tests for the SharedUtils class.
 */
public class SharedUtilsTest {

    /**
     * Mocked logger.
     */
    @Mock
    private Logger loggerMock;


    /**
     * Test for the getExchangeName method.
     * Expects the correct exchange name.
     */
    @Test
    public void testGetExchangeName() {
        // Act
        String exchangeName = SharedUtils.getExchangeName();

        // Assert
        assertEquals("messages", exchangeName);
    }

    /**
     * Test for the getExchangeType method.
     * Expects the correct exchange type.
     */
    @Test
    public void testGetExchangeType() {
        // Act
        String exchangeType = SharedUtils.getExchangeType();

        // Assert
        assertEquals("fanout", exchangeType);
    }
}
