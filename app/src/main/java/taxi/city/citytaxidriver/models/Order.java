package taxi.city.citytaxidriver.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import taxi.city.citytaxidriver.db.models.*;
import taxi.city.citytaxidriver.utils.Constants;

public class Order implements Serializable{

    @Expose
    private int id;

    @Expose
    @SerializedName("client_phone")
    private String clientPhone;

    @Expose
    private OrderStatus status;

    @Expose
    @SerializedName("address_start_name")
    private String startName;

    @Expose
    @SerializedName("address_stop_name")
    private String stopName;

    @Expose
    @SerializedName("address_start")
    private String startPoint;

    @Expose
    @SerializedName("address_stop")
    private String stopPoint;

    @Expose
    @SerializedName("wait_time")
    private String waitTime;

    @Expose
    @SerializedName("wait_time_price")
    private double waitTimePrice;

    @Expose
    @SerializedName("fixed_price")
    private double fixedPrice;

    @Expose
    private Tariff tariff;

    @Expose
    private User driver;

    @Expose
    private User client;

    @Expose
    private String description;

    @Expose
    @SerializedName("order_travel_time")
    private String duration;

    @Expose
    @SerializedName("order_sum")
    private double sum;

    @Expose
    @SerializedName("order_distance")
    private double distance;

    private boolean isActive;

    private double latitude;
    private double longitude;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getStartName() {
        return startName;
    }

    public void setStartName(String startName) {
        this.startName = startName;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getStopPoint() {
        return stopPoint;
    }

    public void setStopPoint(String stopPoint) {
        this.stopPoint = stopPoint;
    }

    public String getWaitTime() {
        return waitTime;
    }

    public long getWaitTimeLong(){
        long res = 0;
        String timeStr = getWaitTime();
        if (timeStr == null || timeStr.equals("null") || timeStr.isEmpty()) return res;
        try {
            String[] list = timeStr.split(":");
            res += 60*60*Integer.valueOf(list[0]) + 60*Integer.valueOf(list[1]) +Integer.valueOf(list[2]);
        } catch (Exception e) {
            res = 0;
        }
        return res;
    }

    public void setWaitTime(String waitTime) {
        this.waitTime = waitTime;
    }

    public void setWaitTime(long seconds) {
        int hr = (int)seconds/3600;
        int rem = (int)seconds%3600;
        int mn = rem/60;
        int sec = rem%60;
        String hrStr = (hr<10 ? "0" : "")+hr;
        String mnStr = (mn<10 ? "0" : "")+mn;
        String secStr = (sec<10 ? "0" : "")+sec;
        this.waitTime = String.format("%s:%s:%s", hrStr, mnStr, secStr);
    }

    public double getWaitTimePrice() {
        if (this.fixedPrice >= Constants.FIXED_PRICE) return 0;
        return tariff.getWaitingRatio() * (double)getWaitTimeLong()/60;
    }

    public void setWaitTimePrice(double waitTimePrice) {
        this.waitTimePrice = waitTimePrice;
    }

    public double getFixedPrice() {
        return fixedPrice;
    }

    public void setFixedPrice(double fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    public Tariff getTariff() {
        return tariff;
    }

    public void setTariff(Tariff tariff) {
        this.tariff = tariff;
    }

    public User getDriver() {
        return driver;
    }

    public void setDriver(User driver) {
        this.driver = driver;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public double getTotalSum() {
        if (this.fixedPrice >= Constants.FIXED_PRICE) return this.fixedPrice;
        return this.getTravelSum() + this.getWaitTimePrice();
    }

    public double getTravelSum() {
        if (this.fixedPrice >= Constants.FIXED_PRICE) return fixedPrice;
        return tariff.getStartPrice() + tariff.getRatio() * distance;
    }

    public boolean isFixedPrice () {
        return this.fixedPrice > Constants.FIXED_PRICE;
    }

    public LatLng getStartPointPosition() {
        String s = this.startPoint;
        String regexPattern = "\\d+\\.?\\d*";
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

    public Order () {}
}
