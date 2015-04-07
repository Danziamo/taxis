package taxi.city.citytaxidriver.Core;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.DecimalFormat;
import java.util.Date;

import taxi.city.citytaxidriver.Enums.OrderStatus;

/**
 * Created by Daniyar on 3/27/2015.
 */
public class Order {
    DecimalFormat df = new DecimalFormat("#.##");

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
    public String address;

    public double distance;
    public double sum;
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
        this.address = client.address;
    }

    private String LatLngToString(LatLng point) {
        if (point != null) {
            String res = point.toString();
            return res.replace("lat/lng", "POINT").replace(",", " ").replace(":", " ");
        } else {
            return "";
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
        obj.put("order_distance", this.distance);
        obj.put("order_sum", this.sum);
        obj.put("order_travel_time", getTimeFromLong(this.time));
        obj.put("description", this.address);

        return obj;
    }

    private String getTimeFromLong(long seconds) {
        int hr = (int)seconds/3600;
        int rem = (int)seconds%3600;
        int mn = rem/60;
        int sec = rem%60;
        String hrStr = (hr<10 ? "0" : "")+hr;
        String mnStr = (mn<10 ? "0" : "")+mn;
        String secStr = (sec<10 ? "0" : "")+sec;
        String res = hrStr + ":" + mnStr +":" + secStr;
        return res;
    }

    public void clear() {
        this.id = 0;
        this.waitTime = null;
        this.startPoint = null;
        this.endPoint = null;
        this.status = null;
        this.tariff = 0;
        this.driver = 0;
        this.orderTime = null;
        this.clientPhone = null;
        this.distance = 0;
        this.time = 0;
        this.sum = 0;
    }
}
