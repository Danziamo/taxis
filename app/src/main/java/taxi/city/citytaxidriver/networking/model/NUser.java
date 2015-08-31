package taxi.city.citytaxidriver.networking.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import taxi.city.citytaxidriver.models.User;

public class NUser{
    @Expose
    public int id;

    @Expose
    public String phone;

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
    @SerializedName("driver_license_number")
    private String driverLicenseNumber;

    public NUser() {
    }

    public NUser(User user) {
        id = user.getId();
        phone = user.getPhone();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        driverLicenseNumber = user.getDriverLicenseNumber();
    }
}
