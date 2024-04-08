package com.jam3s.sharedutils;

import org.junit.Test;


import static org.junit.Assert.assertThrows;

/**
 * The SharedUtilsTest class contains unit tests for the SharedUtils class.
 */
public class SharedUtilsTest {

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
