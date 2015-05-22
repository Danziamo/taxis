package taxi.city.citytaxidriver.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import taxi.city.citytaxidriver.utils.Helper;

/**
 * Created by Daniyar on 3/18/2015.
 */
public class Client implements Serializable{
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
    public String fixedPrice;
    public String totalSum;
    public boolean active;

    public Client() {}

    public Client(JSONObject row, int userId, boolean isActive) throws JSONException {
        this.active = isActive;
        this.phone = row.getString("client_phone");
        this.startPoint = row.getString("address_start");
        this.endPoint = row.getString("address_stop");
        this.driver = userId;
        this.id = row.getInt("id");
        this.waitTime = row.getString("wait_time");
        this.tariff = row.getInt("tariff");
        this.status = row.getString("status");
        this.orderTime = row.getString("order_time");
        this.addressStart = row.getString("address_start_name");
        this.description = row.getString("description");
        this.addressEnd = row.getString("address_stop_name");
        this.sum = row.getString("order_sum");
        this.distance = row.getString("order_distance");
        this.time = row.getString("order_travel_time");
        this.waitSum = row.getString("wait_time_price");
        this.fixedPrice = row.getString("fixed_price");
    }

    public Client(Order order, int driverId, boolean isActive) {
        this.active = isActive;
        this.id = order.id;
        this.phone = order.clientPhone;
        this.driver = driverId;
        this.addressStart = order.addressStart;
        this.addressEnd = order.addressEnd;
        this.fixedPrice = String.valueOf((int)order.fixedPrice);
        this.description = order.description;
        this.distance = String.valueOf(Math.round(100*order.distance)/100);
        this.status = order.status.toString();
        this.waitSum = String.valueOf((int)order.getWaitSum());
        this.waitTime = Helper.getTimeFromLong(order.waitTime);
        this.sum = String.valueOf((int)order.getTravelSum());
        this.totalSum = String.valueOf((int)order.getTotalSum());
    }

    public JSONObject getClientAsJSON() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("driver", this.driver);
        object.put("wait_sum", this.waitSum);
        object.put("wait_time", this.waitTime);
        object.put("order_travel_time", this.time);
        object.put("order_sum", this.sum);
        object.put("distance", this.distance);
        return object;
    }
}
