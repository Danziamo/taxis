package taxi.city.citytaxidriver.models;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class Session implements Serializable {
    @Expose
    private String phone;
    @Expose
    private String password;
    @Expose
    private String token;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
