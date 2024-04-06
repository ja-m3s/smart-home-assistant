package lightBulbMonitor;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class LightBulbMonitorTest {

    @Test
    public void testRetrieveEnvVariable_VariableDoesNotExist_ThrowsException() {
        // Arrange
        String variableName = "NON_EXISTENT_VARIABLE";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            LightBulbMonitor.retrieveEnvVariable(variableName);
        });
    }

}
