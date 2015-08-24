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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public double getWaitingRatio() {
        return waitingRatio;
    }

    public void setWaitingRatio(double waitingRatio) {
        this.waitingRatio = waitingRatio;
    }
}
