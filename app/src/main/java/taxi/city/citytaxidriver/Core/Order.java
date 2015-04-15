package taxi.city.citytaxidriver.Core;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

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
    public long waitTime;
    public int tariff;
    public int driver;
    public String addressStart;
    public String addressEnd;
    public String description;

    public double distance;
    public double sum;
    public long time;
    public double waitSum;

    private Order() {

    }

    public static Order getInstance() {
        if (mInstance == null) {
            mInstance = new Order();
        }
        return mInstance;
    }

    private LatLng getLatLng(String s) {
        if (s == null || s.equals("null"))
            return null;
        String[] geo = s.replace("(", "").replace(")", "").split(" ");

        double latitude = Double.valueOf(geo[1].trim());
        double longitude = Double.valueOf(geo[2].trim());
        return new LatLng(latitude, longitude);
    }

    public void setOrder(Client client) {
        this.id = client.id;
        this.waitTime = 0;
        this.startPoint = getLatLng(client.startPoint);
        this.tariff = client.tariff;
        this.clientPhone = client.phone;
        this.orderTime = client.orderTime;
        this.addressStart = client.addressStart;
        this.description = client.description;
    }

    public void setOrder(JSONObject object) throws JSONException {
        this.id = object.getInt("id");
        this.clientPhone = object.getString("client_phone");
        this.orderTime = object.has("order_time") ? object.getString("order_time") : "00:00:00";
        this.status = object.has("status") ? getStatus(object.getString("status")) : OrderStatus.STATUS.NEW;
        this.startPoint = object.has("address_start") ? stringToLatLng(object.getString("address_start")) : null;
        this.addressStart = object.has("address_start_name") ? object.getString("address_start_name") : null;
        this.tariff = object.has("tariff") ? object.getInt("tariff") : 0;
        this.driver = object.has("driver") ? object.getInt("driver") : 0;
        this.description = object.has("description") ? object.getString("description") : null;
        this.sum = object.has("order_sum") ? object.getDouble("order_sum") : 0;
        this.distance = object.has("order_distance") ? object.getDouble("order_distance") : 0;
        this.time = object.has("order_travel_time") ? getLongFromString(object.getString("order_travel_time")) : 0;
        this.waitSum = object.has("wait_time_price") ? getDoubleFromObject(object.getString("wait_time_price")): 0;
    }

    private Long getLongFromString(String s) {
        long res = 0;
        try {
            String[] list = s.split(":");
            res += 60*60*Integer.valueOf(list[0]) + 60*Integer.valueOf(list[1]) +Integer.valueOf(list[2]);
        } catch (Exception e) {
            res = 0;
        }
        return res;

    }

    private double getDoubleFromObject(String s) {
        double res;
        try {
            res = Double.valueOf(s);
        } catch (Exception e) {
            res = 0;
        }
        return res;
    }

    private OrderStatus.STATUS getStatus(String status) {
        if (status.equals("new"))
            return OrderStatus.STATUS.NEW;
        if (status.equals("accepted"))
            return OrderStatus.STATUS.ACCEPTED;
        if (status.equals("waiting"))
            return OrderStatus.STATUS.WAITING;
        if (status.equals("ontheway"))
            return OrderStatus.STATUS.ONTHEWAY;
        if (status.equals("cancelled"))
            return OrderStatus.STATUS.CANCELED;
        if (status.equals("finished"))
            return OrderStatus.STATUS.FINISHED;
        if (status.equals("sos"))
            return OrderStatus.STATUS.SOS;
        return OrderStatus.STATUS.NEW;
    }

    public void setStatus(String status) {
        if (status.equals("new"))
            this.status = OrderStatus.STATUS.NEW;
        if (status.equals("accepted"))
            this.status = OrderStatus.STATUS.ACCEPTED;
        if (status.equals("waiting"))
            this.status = OrderStatus.STATUS.WAITING;
        if (status.equals("ontheway"))
            this.status = OrderStatus.STATUS.ONTHEWAY;
        if (status.equals("cancelled"))
            this.status = OrderStatus.STATUS.CANCELED;
        if (status.equals("finished"))
            this.status = OrderStatus.STATUS.FINISHED;
        if (status.equals("sos"))
            this.status = OrderStatus.STATUS.SOS;
    }

    public LatLng stringToLatLng(String s) {
        if (s == null || s.equals("null"))
            return null;
        String[] geo = s.replace("(", "").replace(")", "").split(" ");

        double latitude = Double.valueOf(geo[1].trim());
        double longitude = Double.valueOf(geo[2].trim());
        return new LatLng(latitude, longitude);
    }

    private String LatLngToString(LatLng point) {
        if (point != null) {
            String res = point.toString();
            return res.replace("lat/lng", "POINT").replace(",", " ").replace(":", " ");
        } else {
            return "";
        }
    }

    public String getFormattedEndPoint() {
        return this.endPoint != null ? LatLngToString(this.endPoint) : null;
    }

    public String getFormattedStartPoint() {
        return this.startPoint != null ? LatLngToString(this.startPoint) : null;
    }

    public JSONObject getOrderAsJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", this.id);
        obj.put("client_phone", this.clientPhone);
        obj.put("status", this.status);
        obj.put("address_start", LatLngToString(this.startPoint));
        obj.put("address_stop", LatLngToString(this.endPoint));
        obj.put("wait_time", getTimeFromLong(this.waitTime));
        obj.put("wait_time_price", this.waitSum);
        obj.put("tariff", this.tariff);
        obj.put("driver", this.driver);
        obj.put("order_time", this.orderTime);
        obj.put("order_distance", (double)Math.round(this.distance*100)/100);
        obj.put("order_sum", this.sum + this.waitSum);
        obj.put("order_travel_time", getTimeFromLong(this.time));
        obj.put("address_start_name", this.addressStart);
        obj.put("address_stop_name", this.addressEnd);
        obj.put("description", this.description);

        return obj;
    }

    public String getTimeFromLong(long seconds) {
        int hr = (int)seconds/3600;
        int rem = (int)seconds%3600;
        int mn = rem/60;
        int sec = rem%60;
        String hrStr = (hr<10 ? "0" : "")+hr;
        String mnStr = (mn<10 ? "0" : "")+mn;
        String secStr = (sec<10 ? "0" : "")+sec;
        return String.format("%s:%s:%s", hrStr, mnStr, secStr);
    }

    public String getFormattedDistance() {
        return df.format(this.distance);
    }

    public void clear() {
        this.id = 0;
        this.waitTime = 0;
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
        this.addressEnd = null;
        this.addressStart = null;
        this.description = null;
        this.waitSum = 0;
    }
}
