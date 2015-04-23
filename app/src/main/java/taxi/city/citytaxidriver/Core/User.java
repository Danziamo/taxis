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
    }

    public String getToken() {
        return this.token;
    }
}
