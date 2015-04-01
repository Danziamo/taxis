package taxi.city.citytaxidriver.Core;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.util.Date;

import taxi.city.citytaxidriver.Enums.OrderStatus;

/**
 * Created by Daniyar on 3/27/2015.
 */
public class Order {
    private static Order mInstance = null;
    public int id;
    public String orderTime;
    public LatLng startPoint;
    public LatLng endPoint;
    public String clientPhone;
    public OrderStatus.STATUS status;
    public String waitTime;
    public int tariff;
    public int driver;

    public String distance;
    public long time;

    private Order() {

    }

    public static Order getInstance() {
        if (mInstance == null) {
            mInstance = new Order();
        }
        return mInstance;
    }

    public void setOrder(Client client) {
        this.id = client.id;
        this.waitTime = client.waitTime;
        this.startPoint = client.startPoint;
        this.endPoint = client.endPoint;
        this.tariff = client.tariff;
        this.clientPhone = client.phone;
        this.orderTime = client.orderTime;
    }

    private String LatLngToString(LatLng point) {
        if (point != null) {
            String res = point.toString();
            return res.replace("lat/lng", "POINT").replace(",", " ").replace(":", " ");
        } else {
            return null;
        }
    }

    public JSONObject getOrderAsJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", this.id);
        obj.put("client_phone", this.clientPhone);
        obj.put("status", this.status);
        obj.put("address_start", LatLngToString(this.startPoint));
        obj.put("address_stop", LatLngToString(this.endPoint));
        obj.put("wait_time", this.waitTime == null || this.waitTime.equals("null") ? "00:00:00" : this.waitTime);
        obj.put("tariff", this.tariff);
        obj.put("driver", this.driver);
        obj.put("order_time", this.orderTime);

        return obj;
    }

    public void clear() {
        mInstance = null;
        this.id = 0;
        this.waitTime = null;
        this.startPoint = null;
        this.endPoint = null;
        this.status = null;
        this.tariff = 0;
        this.driver = 0;
        this.orderTime = null;
        this.clientPhone = null;

    }
}
