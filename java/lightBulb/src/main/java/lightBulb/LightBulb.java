package lightBulb;

/**
 * This class represents a light bulb with its state and time turned on.
 */
public class LightBulb {

    /**
     * Enum representing the possible states of the light bulb.
     */
    public enum LightBulbState {
        ON("on"),
        OFF("off"),
        TRIGGERED("triggered");

        private final String value;

        LightBulbState(String value) {
            this.value = value;
        }

        /**
         * Gets the value of the state.
         * @return The value of the state
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Represents the current state of the light bulb.
     */
    private LightBulbState state;

    /**
     * Represents the timestamp when the light bulb was turned on.
     */
    private long timeTurnedOn;


    /**
     * Constructs a LightBulb object with initial state ON and current time as the time turned on.
     */
    public LightBulb() {
        super();
        this.state = LightBulbState.ON;
        this.timeTurnedOn = System.currentTimeMillis();
    }

    /**
     * Returns a string representation of the LightBulb object.
     * @return A string representation of the object
     */
    @Override
    public String toString() {
        return "LightBulb [state=" + state + " timeTurnedOn="+timeTurnedOn+"]";
    }

    /**
     * Gets the state of the light bulb.
     * @return The state of the light bulb
     */
    public LightBulbState getState() {
        return this.state;
    }

    /**
     * Sets the state of the light bulb.
     * @param state The state to set
     */
    public void setState(LightBulbState state) {
        this.state = state;
    }

    /**
     * Gets the time when the light bulb was turned on.
     * @return The time when the light bulb was turned on
     */
    public long getTimeTurnedOn() {
        return timeTurnedOn;
    }

    /**
     * Sets the time when the light bulb was turned on.
     * @param timeTurnedOn The time when the light bulb was turned on
     */
    public void setTimeTurnedOn(long timeTurnedOn) {
        this.timeTurnedOn = timeTurnedOn;
    }
}
