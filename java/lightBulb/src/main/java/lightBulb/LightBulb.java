package lightBulb;

public class LightBulb {
	    private State lightBulbState = State.ON;
		private long timeTurnedOn;

		public LightBulb(State lightBulbState) {
			super();
			this.lightBulbState = lightBulbState;
			this.timeTurnedOn = System.currentTimeMillis();
		}

		@Override
		public String toString() {
			return "LightBulb [lightBulbState=" + lightBulbState + "]";
		}

		public String getState(){
			return this.lightBulbState.getValue();
		}
		
		public long getTimeTurnedOn(){
			return timeTurnedOn;
		}
}

