package dbImporter;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class DBImporterTest {

    @Test
    public void testRetrieveEnvVariable_VariableExists_ReturnsValue() {
        // Arrange
        String variableName = "SOME_VARIABLE";
        String expectedValue = "someValue";
        System.setProperty(variableName, expectedValue);

        // Act
        String actualValue = DBImporter.retrieveEnvVariable(variableName);

        // Assert
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testRetrieveEnvVariable_VariableDoesNotExist_ThrowsException() {
        // Arrange
        String variableName = "NON_EXISTENT_VARIABLE";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            DBImporter.retrieveEnvVariable(variableName);
        });
    }
}
