package taxi.city.citytaxidriver.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
    public Car car;
    public float rating;

    private User() {}

    public static User getInstance() {
        if (mInstance == null) {
            mInstance = new User();
        }
        return mInstance;
    }

    public void setUser(JSONObject json) throws JSONException {
        this.car = null;
        this.id = json.getInt("id");
        this.firstName = json.getString("first_name");
        this.lastName = json.getString("last_name");
        this.token = json.has("token") ? json.getString("token") : this.token;
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
        if (json.has("rating")) {
            String ratingSumString = json.getJSONObject("rating").getString("votes__sum");
            double ratingSum = ratingSumString == null || ratingSumString.equals("null") ? 0 : Double.valueOf(ratingSumString);
            int ratingCount = json.getJSONObject("rating").getInt("votes__count");
            this.rating = ratingCount == 0 ? 0 : (float) round(ratingSum / ratingCount, 1);
        }
        JSONArray car = json.getJSONArray("cars");
        if (car.length() > 0) {
            this.car = new Car(car.getJSONObject(0));
        }
    }

    private float round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return (float)bd.doubleValue();
    }

    public void setRating(double a, double b) {
        if (b == 0) {
            this.rating = 0;
        } else {
            this.rating = round(a/b, 1);
        }
    }

    public String getToken() {
        return this.token;
    }
}
