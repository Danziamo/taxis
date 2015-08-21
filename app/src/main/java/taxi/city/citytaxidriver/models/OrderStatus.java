package taxi.city.citytaxidriver.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public enum  OrderStatus implements Serializable {
    @SerializedName("new")
    NEW,
    @SerializedName("accepted")
    ACCEPTED,
    @SerializedName("waiting")
    WAITING,
    @SerializedName("ontheway")
    ONTHEWAY,
    @SerializedName("pending")
    PENDING,
    @SerializedName("finished")
    FINISHED,
    @SerializedName("canceled")
    CANCELED,
    @SerializedName("sos")
    SOS;

    @Override
    public String toString() {
        switch (this) {
            case NEW:
                return "new";
            case ACCEPTED:
                return "accepted";
            case WAITING:
                return "waiting";
            case ONTHEWAY:
                return "ontheway";
            case PENDING:
                return "pending";
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