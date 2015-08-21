package taxi.city.citytaxidriver.nerworking.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AccountActivation {
    @Expose
    public String phone;
    @Expose
    public String password;

    @Expose
    @SerializedName("activation_code")
    public String activationCode;
}
