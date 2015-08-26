package taxi.city.citytaxidriver.db.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import taxi.city.citytaxidriver.models.Order;
import taxi.city.citytaxidriver.models.OrderStatus;

public class OrderModel {
    @Expose
    private int id;

    @Expose
    private OrderStatus status;

    @Expose
    @SerializedName("address_stop")
    private String addressStopPoint;

    @Expose
    private int driver;

    @Expose
    @SerializedName("wait_time")
    private String waitTime;

    @Expose
    @SerializedName("wait_time_price")
    private double waitTimePrice;

    @Expose
    @SerializedName("order_travel_time")
    private String duration;

    @Expose
    @SerializedName("order_sum")
    private double sum;

    @Expose
    @SerializedName("order_distance")
    private double distance;



    public OrderModel() {}

    public OrderModel(Order order) {
        id = order.getId();
        waitTime = order.getWaitTime();
        waitTimePrice = order.getWaitTimePrice();
        duration = order.getDuration();
        sum = order.getSum();
        distance = order.getDistance();
        driver = order.getDriver().getId();
        this.addressStopPoint = order.getStopPoint();
        this.status = order.getStatus();
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getAddressStopPoint() {
        return addressStopPoint;
    }

    public void setAddressStopPoint(String addressStopPoint) {
        this.addressStopPoint = addressStopPoint;
    }

    public int getDriver() {
        return driver;
    }

    public void setDriver(int driver) {
        this.driver = driver;
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
}
