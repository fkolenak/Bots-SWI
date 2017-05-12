package cz.zcu.swi.fkolenak.helpers;

/**
 * Created by japan on 04-May-17.
 */
public class State {


    private HIGH currentStateHigh;
    private LOW currentStateLow;


    public State(HIGH currentStateHigh, LOW currentStateLow) {
        this.currentStateHigh = currentStateHigh;
        this.currentStateLow = currentStateLow;
    }

    public LOW getCurrentStateLow() {
        return currentStateLow;
    }

    public void setCurrentStateLow(LOW currentStateLow) {
        this.currentStateLow = currentStateLow;
    }

    public HIGH getCurrentStateHigh() {
        return currentStateHigh;
    }

    public void setCurrentStateHigh(HIGH currentStateHigh) {
        this.currentStateHigh = currentStateHigh;
    }


    public enum HIGH {
        GEAR_UP_MINIMAL("GEAR_UP_MINIMAL"),
        READY("READY"),
        HEAL("HEAL");

        private final String s;

        HIGH(String text) {
            this.s = text;
        }

        public String toString() {
            return this.s;
        }

    }

    public enum LOW {
        NONE("NONE"),
        GET_WEAPON("GETTING_WEAPON"),
        FLAG_STEAL("STEAL_FLAG"),
        FLAG_STEALING("STEALING_FLAG");

        private final String s;

        LOW(String text) {
            this.s = text;
        }

        public String toString() {
            return this.s;
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("High[");
        b.append(getCurrentStateHigh());
        b.append("]");

        b.append("Low[");
        b.append(getCurrentStateLow());
        b.append("]");

        return b.toString();
    }
}
