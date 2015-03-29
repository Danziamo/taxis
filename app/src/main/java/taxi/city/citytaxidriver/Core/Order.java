package taxi.city.citytaxidriver.Core;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;

/**
 * Created by Daniyar on 3/27/2015.
 */
public class Order {
    private static Order mInstance = null;
    public int id;
    public LatLng startPoint;
    public LatLng endPoint;
    public String clientPhone;
    public String status;
    public long waitTime;
    public int tariff;
    public int driver;

    private Order() {

    }

    public static Order getInstance() {
        if (mInstance == null) {
            mInstance = new Order();
        }
        return mInstance;
    }

    public void clear() {
        mInstance = null;
    }
}
