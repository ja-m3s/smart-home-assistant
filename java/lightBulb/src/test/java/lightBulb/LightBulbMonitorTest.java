package lightBulb;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

/**
 * The LightBulbMonitorTest class contains unit tests for the LightBulbMonitor class.
 */
public class LightBulbMonitorTest {

    /**
     * Tests the retrieveEnvVariable method when the variable does not exist.
     */
    @Test
    public void testRetrieveEnvVariable_VariableDoesNotExist_ThrowsException() {
        // Arrange
        String variableName = "NON_EXISTENT_VARIABLE";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            LightBulbController.retrieveEnvVariable(variableName);
        });
    }

}
