package taxi.city.citytaxidriver.models;

import com.google.gson.annotations.SerializedName;

public enum Role {
    @SerializedName("driver")
    DRIVER,
    @SerializedName("user")
    USER;

    @Override
    public String toString() {
        switch (this) {
            case DRIVER:
                return "driver";
            case USER:
                return "user";
            default:
                return null;
        }
    }
}
