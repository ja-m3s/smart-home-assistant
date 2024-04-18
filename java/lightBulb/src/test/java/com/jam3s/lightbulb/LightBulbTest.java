package com.jam3s.lightbulb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.jam3s.lightbulb.LightBulb.LightBulbState;

public class LightBulbTest {
    /**
     * A lightbulb.
     */
    private static LightBulb lightBulb;

    /**
     * Test setup.
     */
    @BeforeAll
    public static void setUp() {
        lightBulb = new LightBulb();
    }

    /**
     * Initial test state.
     */
    @Test
    public void testInitialState() {
        assertNotNull(lightBulb);
        assertEquals(LightBulbState.ON, lightBulb.getState());
    }

    /**
     * test state setter.
     */
    @Test
    public void testSetState() {
        lightBulb.setState(LightBulbState.OFF);
        assertEquals(LightBulbState.OFF, lightBulb.getState());
    }

    /**
     * test time getter.
     */
    @Test
    public void testGetTimeTurnedOn() {
        long currentTime = System.currentTimeMillis();
        long timeTurnedOn = lightBulb.getTimeTurnedOn();
        // Allow for a small difference due to time taken by tests to execute
        long timeDifference = currentTime - timeTurnedOn;
        assertTrue(timeDifference >= 0 && timeDifference < 100);
    }

    /**
     * test time setter.
     */
    @Test
    public void testSetTimeTurnedOn() {
        long newTime = System.currentTimeMillis() - 10000; // Subtract 10 seconds
        lightBulb.setTimeTurnedOn(newTime);
        assertEquals(newTime, lightBulb.getTimeTurnedOn());
    }

    /**
     * test toString
     */
    @Test
    public void testToString() {
        String expectedString = "LightBulb [state=ON, timeTurnedOn=" + lightBulb.getTimeTurnedOn() + "]";
        assertEquals(expectedString, lightBulb.toString());
    }
}
