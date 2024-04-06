package dbImporter;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.lang.reflect.Field;
import java.util.Map;

public class DBImporterTest {

    @Test
    public void testRetrieveEnvVariable_VariableExists_ReturnsValue() {
        // Arrange
        String variableName = "SOME_VARIABLE";
        String expectedValue = "someValue";
        setEnv(variableName, expectedValue);

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

    public static void setEnv(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
    }
}
