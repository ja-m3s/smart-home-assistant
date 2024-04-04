package lightBulb;

public class LightBulb {

    public enum LightBulbState {
        ON("on"),
        OFF("off"),
        TRIGGERED("triggered");

        private final String value;

        LightBulbState(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private LightBulbState state;
    private long timeTurnedOn;

    public LightBulb() {
        super();
        this.state = LightBulbState.ON;
        this.timeTurnedOn = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "LightBulb [state=" + state + "]";
    }

    public String getState() {
        return this.state.getValue();
    }

    public void setState(LightBulbState state) {
        this.state = state;
    }

    public long getTimeTurnedOn() {
        return timeTurnedOn;
    }

    public void setTimeTurnedOn(long timeTurnedOn) {
        this.timeTurnedOn = timeTurnedOn;
    }
}
