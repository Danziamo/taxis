package taxi.city.citytaxidriver.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import taxi.city.citytaxidriver.core.Client;
import taxi.city.citytaxidriver.core.Order;
import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.enums.OStatus;
import taxi.city.citytaxidriver.service.ApiService;

/**
 * Created by Daniyar on 4/16/2015.
 */
public class Helper {
    private static String regexPattern = "\\d+\\.?\\d*";
    private static DecimalFormat df = new DecimalFormat("#.##");
    private final static String ORDER_PREFS = "OrderPrefsFile";
    private final static String USER_PREFS = "UserPrefsFile";
    private static SharedPreferences settings;

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
        order.addressEnd = object.has("address_stop_name") ? object.getString("address_stop_name") : null;
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
        order.addressEnd = client.addressEnd;
        order.description = client.description;
        double fixedPrice = 0;
        try {
            fixedPrice = Double.valueOf(client.fixedPrice);
        } catch (Exception e) {
            fixedPrice = 0;
        }
        order.fixedPrice = fixedPrice;
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

    public static boolean isSuccess (JSONObject object) throws JSONException {
        if (object == null) return false;
        if (!object.has("status_code")) return false;
        if (!isSuccess(object.getInt("status_code"))) return false;
        return true;
    }

    public static boolean isBadRequest(int status) {
        if (status == HttpStatus.SC_FORBIDDEN) return true;
        if (status == HttpStatus.SC_NOT_FOUND) return true;
        if (status == HttpStatus.SC_NOT_ACCEPTABLE) return true;
        if (status == HttpStatus.SC_METHOD_NOT_ALLOWED) return true;
        if (status == HttpStatus.SC_UNAUTHORIZED) return true;
        if (status == HttpStatus.SC_BAD_REQUEST) return true;
        return false;
    }

    public static boolean isBadRequest(JSONObject object) throws  JSONException {
        if (object == null) return false;
        if (!object.has("status_code")) return false;
        if (!isBadRequest(object.getInt("status_code"))) return false;
        return true;
    }

    public static boolean isOrderPreferenceActive(Context context) {
        settings = context.getSharedPreferences(ORDER_PREFS, 0);
        return settings.contains("orderId") && settings.contains("orderStatus") && !(settings.getInt("orderId", 0) == 0 || settings.getString("orderStatus", null) == null);
    }

    public static void saveOrderPreferences(Context context, Order order) {
        settings = context.getSharedPreferences(ORDER_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("orderDistance", (float)order.distance);
        editor.putInt("orderId", order.id);
        editor.putFloat("orderFixedPrice", (float)order.fixedPrice);
        editor.putLong("orderTime", order.time);
        editor.putLong("orderWaitTime", order.waitTime);
        editor.putString("orderPhone", order.clientPhone);
        editor.putString("orderStartPoint", Helper.getFormattedLatLng(order.startPoint));
        editor.putString("orderStatus", order.status == null ? null : order.status.toString());
        editor.putString("orderStartAddress", order.addressStart);
        editor.putString("orderEndAddress", order.addressEnd);
        editor.putString("orderDescription", order.description);
        editor.apply();
    }

    public static void destroyOrderPreferences(Context context) {
        resetOrderPreferences(context);
        clearOrderPreferences(context);
    }

    public static void clearOrderPreferences(Context context) {
        settings = context.getSharedPreferences(ORDER_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear().apply();
    }

    public static void resetOrderPreferences(Context context) {
        settings = context.getSharedPreferences(ORDER_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("orderId", 0);
        editor.putString("status", null);
        editor.apply();
    }

    public static void getOrderPreferences(Context context) {
        settings = context.getSharedPreferences(ORDER_PREFS, 0);
        Order order = Order.getInstance();
        if (!settings.contains("orderId")) return;
        String pStatus = settings.getString("orderStatus", "");
        if (pStatus.equals(OStatus.ONTHEWAY.toString()))
            order.status = OStatus.PENDING;
        else
            order.status = Helper.getStatus(pStatus);

        order.id = settings.getInt("orderId", 0);
        order.clientPhone = settings.getString("orderPhone", null);
        order.time = settings.getLong("orderTime", 0);
        order.fixedPrice = (double)settings.getFloat("orderFixedPrice", 0);
        order.distance = (double)settings.getFloat("orderDistance", 0);
        order.waitTime = settings.getLong("orderWaitTime", 0);
        order.startPoint = Helper.getLatLng(settings.getString("orderStartPoint", null));
        order.addressStart = settings.getString("orderStartAddress", null);
        order.addressEnd = settings.getString("orderEndAddress", null);
    }

    public static void getUserPreferences(Context context) {
        settings = context.getSharedPreferences(USER_PREFS, 0);
        User user = User.getInstance();
        user.id = settings.getInt("id", 0);
        user.phone = settings.getString("phone", null);
        user.password = settings.getString("password", null);
        user.token = settings.getString("token", null);
        user.deviceToken = settings.getString("deviceToken", null);
        user.firstName = settings.getString("first_name", null);
        user.lastName = settings.getString("last_name", null);
        user.email = settings.getString("email", null);
        user.balance = (double)settings.getInt("balance", 0);
        user.passportNumber = settings.getString("passport_number", null);
        user.driverLicenseNumber = settings.getString("license_number", null);
        user.dob = settings.getString("dob", null);
        user.address = settings.getString("address", null);
        ApiService.getInstance().setToken(user.getToken());
    }

    public static void saveUserPreferences(Context context, User user) {
        settings = context.getSharedPreferences(USER_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("id", user.id);
        editor.putString("phone", user.phone);
        editor.putString("password", user.password);
        editor.putString("token", user.getToken());
        editor.putString("deviceToken", user.deviceToken);
        editor.putString("first_name", user.firstName);
        editor.putString("last_name", user.lastName);
        editor.putString("email", user.email);
        editor.putInt("balance", (int) user.balance);
        editor.putString("passport_number", user.passportNumber);
        editor.putString("license_number", user.driverLicenseNumber);
        editor.putString("dob", user.dob);
        editor.putString("address", user.address);
        editor.apply();
    }

    public static double getDouble(String number) {
        double result = 0;
        try {
            result = Double.valueOf(number);
        } catch (Exception e) {
            result = 0;
        }
        return result;
    }

    public static String getStringFromJson(JSONObject object, String key) throws JSONException{
        if (!object.has(key)) return null;
        String value = object.getString(key);
        if (value == null || value.equals("null")) return null;
        return value;
    }

    public static boolean isOrderActive(Order order) {
        if (order == null || order.id == 0) return false;
        OStatus status = order.status;
        if (status == null) return false;
        if (status == OStatus.NEW) return false;
        if (status == OStatus.FINISHED) return false;
        if (status == OStatus.CANCELED) return false;
        return true;
    }

    public static boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public static boolean isYearValid(String s) {
        int year = 0;
        try {
            year = Integer.valueOf(s);
        } catch (Exception e) {
            return false;
        }
        return !(year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR));
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
