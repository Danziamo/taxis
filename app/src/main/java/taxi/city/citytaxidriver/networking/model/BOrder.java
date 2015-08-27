package taxi.city.citytaxidriver.networking.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import taxi.city.citytaxidriver.db.models.OrderModel;
import taxi.city.citytaxidriver.models.Order;
import taxi.city.citytaxidriver.models.OrderStatus;
import taxi.city.citytaxidriver.utils.Constants;

public class BOrder {
    @Expose
    public int driver;

    @Expose
    @SerializedName("address_start_name")
    public String startName;

    @Expose
    @SerializedName("address_stop_name")
    public String stopName;

    @Expose
    @SerializedName("address_start")
    public String startPoint;

    @Expose
    @SerializedName("address_stop")
    public String stopPoint;

    @Expose
    public int tariff;

    @Expose
    public OrderStatus status;

    @Expose
    @SerializedName("client_phone")
    public String clientPhone;

    public BOrder(Order order) {
        tariff = order.getTariff().getTariffId();
        status = order.getStatus();
        startName = order.getStartName();
        stopName = order.getStopName();
        startPoint = order.getStartPoint();
        stopPoint = order.getStopPoint();
        driver = order.getDriver().getId();
        clientPhone = order.getClientPhone();
    }

    public BOrder(OrderModel order) {
        tariff = order.getTariffId();
        status = order.getStatus();
        startName = order.getStartName();
        stopName = order.getStopName();
        startPoint = order.getStartPoint();
        stopPoint = order.getStopPoint();
        driver = order.getDriverId();
        clientPhone = order.getClientPhone();
    }
}
