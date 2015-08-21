package taxi.city.citytaxidriver.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Tariff implements Serializable {

    @Expose
    private  int id;

    @Expose
    @SerializedName("tariff_name")
    private String name;

    @Expose
    @SerializedName("seat_in_car_price")
    private double startPrice;

    @Expose
    @SerializedName("kilometer_price")
    private double ratio;

    @Expose
    @SerializedName("waiting_between_point_price")
    private double waitingRatio;
}
