package taxi.city.citytaxidriver.Core;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by Daniyar on 3/18/2015.
 */
public class Client implements Serializable, Comparable<Client>{
    public int id;
    public String orderTime;
    public String phone;
    public String startPoint;
    public String endPoint;
    public String waitTime;
    public String status;
    public int tariff;
    public int driver;
    public String addressStart;
    public String addressEnd;
    public String description;
    public String sum;
    public String time;
    public String distance;
    public String waitSum;

    public Client() {}

    @Override
    public int compareTo(Client another) {
        if (another.status == "accepted" || another.status == "ontheway" || another.status == "waiting")
            return 0;
        if (another.status == "new")
            return -1;
        else
            return 1;

    }
}
