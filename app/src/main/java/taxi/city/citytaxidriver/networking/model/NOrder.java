package taxi.city.citytaxidriver.networking.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import taxi.city.citytaxidriver.models.Order;
import taxi.city.citytaxidriver.models.OrderStatus;

public class NOrder {
    @Expose
    public int id;

    @Expose
    public OrderStatus status;

    @Expose
    @SerializedName("address_stop")
    public String addressStopPoint;

    @Expose
    public int driver;

    @Expose
    @SerializedName("wait_time")
    public String waitTime;

    @Expose
    @SerializedName("wait_time_price")
    public double waitTimePrice;

    @Expose
    @SerializedName("order_travel_time")
    public String duration;

    @Expose
    @SerializedName("order_sum")
    public double sum;

    @Expose
    @SerializedName("order_distance")
    public double distance;

    public NOrder () {}

    public NOrder (Order order) {
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
}
