package lightBulb.jar;

public enum State {
    ON("on"),
    ACTIVE("active"),
    TRIGGERED("triggered");

    private final String value;

    State(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

