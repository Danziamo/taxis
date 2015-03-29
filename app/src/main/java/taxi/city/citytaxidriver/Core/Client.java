package taxi.city.citytaxidriver.Core;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by Daniyar on 3/18/2015.
 */
public class Client implements Serializable{
    public String name;
    public String phone;
    public LatLng startPoint;

    public Client() {};

    public Client(String name, String phone, LatLng startPoint) {
        this.name = name;
        this.phone = phone;
        this.startPoint = startPoint;
    }
}
