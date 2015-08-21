package taxi.city.citytaxidriver.networking.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import taxi.city.citytaxiclient.models.OrderStatus;

public class NOrder {

    @Expose
    @SerializedName("client_phone")
    public String clientPhone;

    public OrderStatus status;

    @Expose
    public int tariff;

    @Expose
    public int client;

    @Expose
    @SerializedName("fixed_price")
    public double fixedPrice;

    @Expose
    @SerializedName("adress_start_name")
    public String  startName;

    @Expose
    @SerializedName("adress_stop_name")
    public String stopName;

    @Expose
    @SerializedName("adress_start")
    public String startPoint;
}
