package taxi.city.citytaxidriver.Core;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Daniyar on 3/26/2015.
 */
public class User implements Serializable {

    private static User mInstance = null;

    public Integer id;
    public String firstName;
    public String lastName;
    public String token;
    public String phone;
    public String password;

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
    }

    public String getToken() {
        return this.token;
    }
}
