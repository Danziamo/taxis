package taxi.city.citytaxidriver.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public enum  OnlineStatus implements Serializable {
        @SerializedName("online")
        ONLINE,
        @SerializedName("offline")
        OFFLINE,
        @SerializedName("exited")
        EXITED;

        @Override
        public String toString() {
            switch (this) {
                case ONLINE:
                    return "online";
                case OFFLINE:
                    return "offline";
                case EXITED:
                    return "exited";
                default:
                    return null;
            }
        }
}
