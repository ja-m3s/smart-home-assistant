package com.jam3s.sharedutils;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
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
     * Setup method to initialize mocks.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

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
