package taxi.city.citytaxidriver.Utils;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import taxi.city.citytaxidriver.Core.Client;
import taxi.city.citytaxidriver.Core.Order;
import taxi.city.citytaxidriver.Enums.OStatus;

/**
 * Created by Daniyar on 4/16/2015.
 */
public class Helper {
    private static String regexPattern = "\\d+\\.?\\d*";
    private static DecimalFormat df = new DecimalFormat("#.##");

    /**
     * Returns string representation of given LatLng to save or send to remote server
     */
    public static String getFormattedLatLng(LatLng location) {
        if (location != null) {
            return "POINT (" + location.latitude + " " + location.longitude + ")";
        }
        return null;
    }

    /**
     * Returns LatLng representation from string
     */
    public static LatLng getLatLng(String s) {
        if (s == null || s.equals("null"))
            return null;
        List<String> geo = new ArrayList<>();
        Matcher m = Pattern.compile(regexPattern).matcher(s);
        while(m.find()) {
            geo.add(m.group());
        }
        if (geo.size() != 2)
            return null;
        double latitude = Double.valueOf(geo.get(0).trim());
        double longitude = Double.valueOf(geo.get(1).trim());
        return new LatLng(latitude, longitude);
    }

    /**
     * Return proper Enum from string
     */
    public static OStatus getStatus(String s) {
        if (s.equals(OStatus.NEW.toString())) return OStatus.NEW;
        if (s.equals(OStatus.ACCEPTED.toString())) return OStatus.ACCEPTED;
        if (s.equals(OStatus.PENDING.toString())) return OStatus.PENDING;
        if (s.equals(OStatus.CANCELED.toString())) return OStatus.CANCELED;
        if (s.equals(OStatus.FINISHED.toString())) return OStatus.FINISHED;
        if (s.equals(OStatus.ONTHEWAY.toString())) return OStatus.ONTHEWAY;
        if (s.equals(OStatus.SOS.toString())) return OStatus.SOS;
        if (s.equals(OStatus.WAITING.toString())) return OStatus.WAITING;
        return null;
    }

    /**
     * Returns formatted time value from long as string "hh:mm:ss"
     */
    public static String getTimeFromLong(long seconds) {
        int hr = (int)seconds/3600;
        int rem = (int)seconds%3600;
        int mn = rem/60;
        int sec = rem%60;
        String hrStr = (hr<10 ? "0" : "")+hr;
        String mnStr = (mn<10 ? "0" : "")+mn;
        String secStr = (sec<10 ? "0" : "")+sec;
        return String.format("%s:%s:%s", hrStr, mnStr, secStr);
    }

    public static String getTimeFromLong(long seconds, OStatus status) {
        if (status == OStatus.NEW || status == OStatus.ACCEPTED || status == OStatus.WAITING) {
            return "00:00:00";
        }
        return getTimeFromLong(seconds);
    }

    /**
     * Saving instance of a order from JSONObject
     */
    public static void setOrder(JSONObject object) throws JSONException {
        Order order = Order.getInstance();
        order.id = object.getInt("id");
        order.clientPhone = object.getString("client_phone");
        order.orderTime = object.has("order_time") ? object.getString("order_time") : "00:00:00";
        order.status = object.has("status") ? getStatus(object.getString("status")) : OStatus.NEW;
        order.startPoint = object.has("address_start") ? getLatLng(object.getString("address_start")) : null;
        order.addressStart = object.has("address_start_name") ? object.getString("address_start_name") : null;
        order.tariff = object.has("tariff") ? object.getInt("tariff") : 0;
        order.driver = object.has("driver") ? object.getInt("driver") : 0;
        order.description = object.has("description") ? object.getString("description") : null;
        order.sum = object.has("order_sum") ? object.getDouble("order_sum") : 0;
        order.distance = object.has("order_distance") ? object.getDouble("order_distance") : 0;
        order.time = object.has("order_travel_time") ? getLongFromString(object.getString("order_travel_time")) : 0;
        order.waitSum = object.has("wait_time_price") ? getLongFromString(object.getString("wait_time_price")): 0;
        order.fixedPrice = object.has("fixed_price") ? object.getDouble("fixed_price") : 0;
    }

    /**
     * Saving instance of the order from given client class
     */
    public static void setOrder(Client client) {
        Order order = Order.getInstance();
        order.id = client.id;
        order.waitTime = 0;
        order.startPoint = getLatLng(client.startPoint);
        order.tariff = client.tariff;
        order.clientPhone = client.phone;
        order.orderTime = client.orderTime;
        order.addressStart = client.addressStart;
        order.description = client.description;
        order.fixedPrice = Double.valueOf(client.fixedPrice);
    }

    /**
     * Returning formatted string representation of distance like #.##
     */
    public static String getFormattedDistance(double distance) {
        return df.format(distance);
    }

    /**
     * Getting long representation of time from string (from hh:mm:ss to seconds)
     */
    public static Long getLongFromString(String s) {
        long res = 0;
        try {
            String[] list = s.split(":");
            res += 60*60*Integer.valueOf(list[0]) + 60*Integer.valueOf(list[1]) +Integer.valueOf(list[2]);
        } catch (Exception e) {
            res = 0;
        }
        return res;

    }

    public double getDoubleFromObject(String s) {
        double res;
        try {
            res = Double.valueOf(s);
        } catch (Exception e) {
            res = 0;
        }
        return res;
    }

    public static boolean isSuccess(int status) {
        if (status == HttpStatus.SC_OK) return true;
        if (status == HttpStatus.SC_ACCEPTED) return true;
        if (status == HttpStatus.SC_CREATED) return true;
        return false;
    }
}
