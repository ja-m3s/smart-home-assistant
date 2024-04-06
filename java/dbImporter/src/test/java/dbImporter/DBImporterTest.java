package dbImporter;

import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class DBImporterTest {


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
