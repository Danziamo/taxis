package taxi.city.citytaxidriver.Core;

import java.io.Serializable;

/**
 * Created by Daniyar on 3/18/2015.
 */
public class Client implements Serializable{
    private String name;
    private String phone;

    public Client() {};

    public Client(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
