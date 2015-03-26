package taxi.city.citytaxidriver.Core;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Daniyar on 3/26/2015.
 */
public class User implements Serializable {
    private String firstName;
    private String lastName;
    private String token;
    private String phone;
    private String password;

    public User(JSONObject json, String phone, String password) throws JSONException {
        firstName = json.getString("first_name");
        lastName = json.getString("last_name");
        token = json.getString("token");
        this.phone = phone;
        this.password = password;
    }

    public String getToken() {
        return this.token;
    }
}
