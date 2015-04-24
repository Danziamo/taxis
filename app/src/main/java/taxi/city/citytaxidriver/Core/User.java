package taxi.city.citytaxidriver.Core;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class User implements Serializable {

    private static User mInstance = null;

    public int id;
    public String firstName;
    public String lastName;
    public String token;
    public String phone;
    public String password;
    public double balance;
    public String passportNumber;
    public String driverLicenseNumber;
    public String email;
    public String dob;
    public String address;
    public String deviceToken;

    private User() {}

    public static User getInstance() {
        if (mInstance == null) {
            mInstance = new User();
        }
        return mInstance;
    }

    public void setUser(JSONObject json) throws JSONException {
        this.id = json.getInt("id");
        this.firstName = json.getString("first_name");
        this.lastName = json.getString("last_name");
        this.token = json.getString("token");
        this.phone = json.getString("phone");
        this.balance = json.getDouble("balance");
        this.email = json.getString("email");
        this.passportNumber = json.getString("passport_number");
        this.driverLicenseNumber = json.getString("driver_license_number");
        this.dob = json.getString("date_of_birth");
        this.address = json.getString("address");
    }

    public String getToken() {
        return this.token;
    }
}
