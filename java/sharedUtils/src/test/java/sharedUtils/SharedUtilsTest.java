package sharedUtils;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

/**
 * The SharedUtilsTest class contains unit tests for the SharedUtils class.
 */
public class SharedUtilsTest {

    /**
     * Tests the retrieveEnvVariable method when the variable does not exist.
     */
    @Test
    public void testRetrieveEnvVariable_VariableDoesNotExist_ThrowsException() {
        // Arrange
        String variableName = "NON_EXISTENT_VARIABLE";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            SharedUtils.retrieveEnvVariable(variableName);
        });
    }

}
