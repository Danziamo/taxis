package taxi.city.citytaxidriver.db.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import taxi.city.citytaxidriver.models.Order;
import taxi.city.citytaxidriver.models.OrderStatus;
import taxi.city.citytaxidriver.models.User;
import taxi.city.citytaxidriver.utils.Constants;
import taxi.city.citytaxidriver.utils.Helper;

@Table(name="orders")
public class OrderModel extends Model implements Serializable {
    @Expose
    @SerializedName("id")
    @Column(name = "order_id")
    private int orderId;

    @Expose
    @SerializedName("client_phone")
    @Column(name = "client_phone")
    private String clientPhone;

    @Expose
    @SerializedName("order_time")
    @Column(name = "order_time")
    private String orderTime;

    @Expose
    @SerializedName("status")
    @Column(name = "status")
    private OrderStatus status;

    @Expose
    @SerializedName("address_start_name")
    @Column(name = "address_start_name")
    private String startName;

    @Expose
    @SerializedName("address_stop_name")
    @Column(name = "address_stop_name")
    private String stopName;

    @Expose
    @SerializedName("address_start")
    @Column(name = "address_start")
    private String startPoint;

    @Expose
    @SerializedName("address_stop")
    @Column(name = "address_stop")
    private String stopPoint;

    @Expose
    @SerializedName("wait_time")
    @Column(name = "wait_time")
    private String waitTime;

    @Expose
    @SerializedName("wait_time_price")
    @Column(name = "wait_time_price")
    private double waitTimePrice;

    @Expose
    @SerializedName("fixed_price")
    @Column(name = "fixed_price")
    private double fixedPrice;

    @Expose
    @SerializedName("tariff")
    @Column(name = "tariff")
    private int tariffId;

    @Expose
    @SerializedName("driver")
    @Column(name = "driver")
    private int driverId;

    //@Expose
    @SerializedName("client")
    @Column(name = "client")
    private int clientId;

    @Expose
    @SerializedName("description")
    @Column(name = "description")
    private String description;

    @Expose
    @SerializedName("order_travel_time")
    @Column(name = "order_travel_time")
    private String orderTravelTime;

    private long duration;

    @Expose
    @SerializedName("order_sum")
    @Column(name = "order_sum")
    private double sum;

    @Expose
    @SerializedName("order_distance")
    @Column(name = "order_distance")
    private double distance;



    public OrderModel() {}

    public OrderModel(Order order) {
        orderId = order.getId();
        clientPhone = order.getClientPhone();
        orderTime = order.getOrderTime();
        status = order.getStatus();
        startName = order.getStartName();
        stopName = order.getStopName();
        startPoint = order.getStartPoint();
        stopPoint = order.getStopPoint();
        waitTime = order.getWaitTime();
        waitTimePrice = order.getWaitTimePrice();
        fixedPrice = order.getFixedPrice();

        Tariff tariff = order.getTariff();
        if(tariff == null){
            tariffId = 1;
        }else {
            tariffId = tariff.getTariffId();
        }

        User driver = order.getDriver();
        if(driver == null){
            driverId = 0;
        }else {
            driverId = order.getDriver().getId();
        }

        clientId = order.getClientId();
        description = order.getDescription();
        orderTravelTime = order.getOrderTravelTime();
        duration = order.getDuration();
        sum = order.getSum();
        distance = order.getDistance();
    }


    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
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
        if (waitTime == null) {
            return "00:00:00";
        }
        return waitTime;
    }

    public void setWaitTime(String waitTime) {
        this.waitTime = waitTime;
    }

    public double getWaitTimePrice() {
        return waitTimePrice;
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

    public int getTariffId() {
        return tariffId;
    }

    public void setTariffId(int tariffId) {
        this.tariffId = tariffId;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrderTravelTime() {
        return orderTravelTime;
    }

    public void setOrderTravelTime(String orderTravelTime) {
        this.orderTravelTime = orderTravelTime;
        this.duration = Helper.getLongFromString(orderTravelTime);
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
        this.orderTravelTime = Helper.getTimeFromLong(duration);
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
        this.distance = (double)Math.round(distance*100)/100;
    }
    //End getter and setters

    //Helper methods
    public Tariff getTariff(){
        return Tariff.getTariffById(tariffId);
    }
    public double getTotalSum() {
        if (isFixedPrice()){
            return this.fixedPrice;
        }
        return this.getTravelSum() + this.getWaitTimePrice();
    }

    public double getTravelSum() {
        if (isFixedPrice()){
            return fixedPrice;
        }
        Tariff tariff = getTariff();
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
    //End Helper methods


    //DB functions
    public long save(Order order){
        orderId = order.getId();
        clientPhone = order.getClientPhone();
        orderTime = order.getOrderTime();
        status = order.getStatus();
        startName = order.getStartName();
        stopName = order.getStopName();
        startPoint = order.getStartPoint();
        stopPoint = order.getStopPoint();
        waitTime = order.getWaitTime();
        waitTimePrice = order.getWaitTimePrice();
        fixedPrice = order.getFixedPrice();

        Tariff tariff = order.getTariff();
        if(tariff == null){
            tariffId = 1;
        }else {
            tariffId = tariff.getTariffId();
        }

        User driver = order.getDriver();
        if(driver == null){
            driverId = 0;
        }else {
            driverId = order.getDriver().getId();
        }

        clientId = order.getClientId();
        description = order.getDescription();
        orderTravelTime = order.getOrderTravelTime();
        duration = order.getDuration();
        sum = order.getSum();
        distance = order.getDistance();
        return super.save();
    }

    public static List<OrderModel> getAllFinishedOrders(){
        return new Select()
                .from(OrderModel.class)
                .where("status = ?", OrderStatus.FINISHED.toString().toUpperCase())
                .execute();
    }

    public static OrderModel getUserLastActiveOrder(int userId) {
        return new Select()
                .from(OrderModel.class)
                .where("status != ?", OrderStatus.FINISHED.toString().toUpperCase())
                .where("driver = ?", userId)
                .orderBy("id DESC")
                .executeSingle();
    }

    public static OrderModel getByOrderId(int orderId){
        return new Select()
                .from(OrderModel.class)
                .where("order_id = ?", orderId)
                .executeSingle();
    }

    public static OrderModel getById(int id){
        return new Select()
                .from(OrderModel.class)
                .where("id = ?", id)
                .executeSingle();
    }

    //End DB functions

}
