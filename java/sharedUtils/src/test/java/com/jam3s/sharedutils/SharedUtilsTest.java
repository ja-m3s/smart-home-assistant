package com.jam3s.sharedutils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the SharedUtils class.
 */
public class SharedUtilsTest {

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
