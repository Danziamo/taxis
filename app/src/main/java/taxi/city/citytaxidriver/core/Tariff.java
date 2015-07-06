package taxi.city.citytaxidriver.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Tariff implements Serializable{
    public String name;
    public double startPrice;
    public double ratio;
    public long waitTime;
    public double waitRatio;

    public Tariff(JSONObject json) throws JSONException {
        this.name = json.getString("tariff_name");
        this.startPrice = json.getDouble("seat_in_car_price");
        this.ratio = json.getDouble("kilometer_price");
        this.waitTime = getLongFromString(json.getString("waiting_to_order"));
        this.waitRatio = json.getDouble("waiting_between_point_price");
    }

    public Tariff() {
        this.name = "Basic";
        this.startPrice = 40;
        this.ratio = 12;
        this.waitTime = 0;
        this.waitRatio = 0;
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
}
