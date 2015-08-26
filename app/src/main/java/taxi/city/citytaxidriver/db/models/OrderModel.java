package taxi.city.citytaxidriver.db.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import taxi.city.citytaxidriver.models.Order;
import taxi.city.citytaxidriver.models.OrderStatus;
import taxi.city.citytaxidriver.models.User;

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

    @Expose
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
    private String duration;

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
    //End getter and setters

    public static List<OrderModel> getAllFinishedOrders(){
        return new Select()
                .from(OrderModel.class)
                .where("status = ?", OrderStatus.FINISHED)
                .execute();
    }

}
