package com.jam3s.lightbulb;

/**
 * This class represents a light bulb with its state and time turned on.
 */
public class LightBulb {

    /**
     * Enum representing the possible states of the light bulb.
     */
    public enum LightBulbState {
        /**
         * Bulb is on.
         */
        ON("on"),
        /**
         * Bulb is off.
         */
        OFF("off"),
        /**
         * Bulb is triggered by the light bulb monitor.
         */
        TRIGGERED("triggered");

        /**
         * Holds light bulb state.
         */
        private final String value;

        /**
         * @param value holds lightbulb state.
         */
        LightBulbState(final String newValue) {
            this.value = newValue;
        }

        /**
         * Gets the value of the state.
         * 
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
     * Constructs a LightBulb object with initial state ON and current time as the
     * time turned on.
     */
    public LightBulb() {
        super();
        this.state = LightBulbState.ON;
        this.timeTurnedOn = System.currentTimeMillis();
    }

    /**
     * Returns a string representation of the LightBulb object.
     *
     * @return A string representation of the object
     */
    @Override
    public String toString() {
        return "LightBulb [state=" + state + ", timeTurnedOn=" + timeTurnedOn + "]";
    }

    /**
     * Gets the state of the light bulb.
     *
     * @return The state of the light bulb.
     */
    public LightBulbState getState() {
        return this.state;
    }

    /**
     * Sets the state of the light bulb.
     *
     * @param newState The state to set.
     */
    public void setState(final LightBulbState newState) {
        this.state = newState;
    }

    /**
     * Gets the time when the light bulb was turned on.
     *
     * @return The time when the light bulb was turned on.
     */
    public long getTimeTurnedOn() {
        return timeTurnedOn;
    }

    /**
     * Sets the time when the light bulb was turned on.
     *
     * @param newTimeTurnedOn The time when the light bulb was turned on.
     */
    public void setTimeTurnedOn(final long newTimeTurnedOn) {
        this.timeTurnedOn = newTimeTurnedOn;
    }
}
