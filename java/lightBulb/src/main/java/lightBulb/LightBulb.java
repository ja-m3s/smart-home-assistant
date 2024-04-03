package lightBulb;

public class LightBulb {
	    private State lightBulbState = State.ON;

		public LightBulb(State lightBulbState) {
			super();
			this.lightBulbState = lightBulbState;
		}

		@Override
		public String toString() {
			return "LightBulb [lightBulbState=" + lightBulbState + "]";
		}
}

