package taxi.city.citytaxidriver.Core;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

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

    public Client() {}

    public Client(JSONObject row, int userId) throws JSONException {
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
    }
}
