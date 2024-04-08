package com.jam3s.dbimporter;

import org.junit.Test;

import com.jam3s.sharedutils.SharedUtils;

import static org.junit.Assert.assertThrows;

/**
 * The DBImporterTest class contains unit tests for the DBImporter class.
 */
public class DBImporterTest {

    /**
     * Private Constructor
     */
    private DBImporterTest(){
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
