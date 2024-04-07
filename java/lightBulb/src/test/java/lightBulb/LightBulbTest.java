package lightBulb;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;

/**
 * The LightBulbTest class contains unit tests for the LightBulb class.
 */
public class LightBulbTest {

    private LightBulb lightBulb;

    /**
     * Sets up the test fixture.
     */
    @Before
    public void setUp() {
        lightBulb = new LightBulb();
    }

    /**
     * Tests the initial state of the light bulb.
     */
    @Test
    public void testInitialState() {
        assertEquals(LightBulb.LightBulbState.ON, lightBulb.getState());
    }

    /**
     * Tests the setState method.
     */
    @Test
    public void testSetState() {
        lightBulb.setState(LightBulb.LightBulbState.OFF);
        assertEquals(LightBulb.LightBulbState.OFF, lightBulb.getState());
    }

    /**
     * Tests the toString method.
     */
    @Test
    public void testToString() {
        String expectedString = "LightBulb [state=ON]";
        assertEquals(expectedString, lightBulb.toString());
    }

    /**
     * Tests the getTimeTurnedOn and setTimeTurnedOn methods.
     */
    @Test
    public void testTimeTurnedOn() {
        long currentTime = System.currentTimeMillis();
        lightBulb.setTimeTurnedOn(currentTime);
        assertEquals(currentTime, lightBulb.getTimeTurnedOn());
    }
}
