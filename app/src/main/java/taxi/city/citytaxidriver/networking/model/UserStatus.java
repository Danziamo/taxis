package taxi.city.citytaxidriver.networking.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import taxi.city.citytaxidriver.models.OnlineStatus;
import taxi.city.citytaxidriver.models.Role;

public class UserStatus {
    @Expose
    @SerializedName("online_status")
    public OnlineStatus onlineStatus;

    @Expose
    @SerializedName("ios_token")
    public String iosToken;

    @Expose
    public Role role;
}
