package lightBulb;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;
import lightBulb.LightBulb.LightBulbState;

public class LightBulbTest {

    private LightBulb lightBulb;

    @Before
    public void setUp() {
        lightBulb = new LightBulb();
    }

    @Test
    public void testInitialState() {
        assertEquals(LightBulbState.ON.getValue(), lightBulb.getState());
    }

    @Test
    public void testSetState() {
        lightBulb.setState(LightBulbState.OFF);
        assertEquals(LightBulbState.OFF.getValue(), lightBulb.getState());
    }

    @Test
    public void testToString() {
        String expectedString = "LightBulb [state=ON]";
        assertEquals(expectedString, lightBulb.toString());
    }

    @Test
    public void testTimeTurnedOn() {
        long currentTime = System.currentTimeMillis();
        lightBulb.setTimeTurnedOn(currentTime);
        assertEquals(currentTime, lightBulb.getTimeTurnedOn());
    }
}
