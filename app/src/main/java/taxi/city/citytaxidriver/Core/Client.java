package taxi.city.citytaxidriver.Core;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by Daniyar on 3/18/2015.
 */
public class Client implements Serializable{
    public int id;
    public String orderTime;
    public String phone;
    public LatLng startPoint;
    public LatLng endPoint;
    public String waitTime;
    public String status;
    public int tariff;
    public int driver;

    public Client() {};
}
