package taxi.city.citytaxidriver.networking.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NUser {
    @Expose
    @SerializedName("first_name")
    public String firstName;

    @Expose
    @SerializedName("last_name")
    public String lastName;

    @Expose
    @SerializedName("password")
    public String password;

    @Expose
    @SerializedName("date_of_birth")
    public String dob;

    @Expose
    public String email;
}
