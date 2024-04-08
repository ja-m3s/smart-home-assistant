package com.jam3s.lightbulbmonitor;

import org.junit.Test;

import com.jam3s.sharedutils.SharedUtils;

import static org.junit.Assert.assertThrows;

/**
 * The LightBulbMonitorTest class contains unit tests for the LightBulbMonitor
 * class.
 */
public class LightBulbMonitorTest {

    /**
     * Public Constructor
     * To satisfy JavaDoc plugin
     */
    public LightBulbMonitorTest(){
    }
    /**
     * Tests the getEnvVar method when the variable does not exist.
     */
    @Test
    public void testgetEnvVar_VariableDoesNotExist_ThrowsException() {
        // Arrange
        String variableName = "NON_EXISTENT_VARIABLE";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            SharedUtils.getEnvVar(variableName);
        });
    }

}
