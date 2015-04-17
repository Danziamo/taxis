package taxi.city.citytaxidriver.Enums;

/**
 * Created by Daniyar on 4/16/2015.
 */

public enum OStatus {
    NEW,
    ACCEPTED,
    ONPLACE,
    ONTHEWAY,
    WAITING,
    SOS,
    CANCELED,
    FINISHED;

    @Override
    public String toString() {
        switch (this) {
            case NEW:
                return "new";
            case ACCEPTED:
                return "accepted";
            case ONPLACE:
                return "waiting";
            case ONTHEWAY:
                return "ontheway";
            case WAITING:
                return "waiting";
            case SOS:
                return "sos";
            case CANCELED:
                return "canceled";
            case FINISHED:
                return "finished";
            default:
                return null;
        }
    }
}
