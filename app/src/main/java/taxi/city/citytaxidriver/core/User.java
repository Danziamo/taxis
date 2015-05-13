package taxi.city.citytaxidriver.core;

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
        String passportNumber = json.getString("passport_number");
        String driverLicenseNumber = json.getString("driver_license_number");
        passportNumber = passportNumber == null || passportNumber.equals("null") ? null : passportNumber;
        driverLicenseNumber = driverLicenseNumber == null || driverLicenseNumber.equals("null") ? null : driverLicenseNumber;
        this.passportNumber = passportNumber;
        this.driverLicenseNumber = driverLicenseNumber;
        this.dob = json.getString("date_of_birth");
        this.address = json.getString("address");
        this.deviceToken = json.getString("android_token");
    }

    public String getToken() {
        return this.token;
    }
}
