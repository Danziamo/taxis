package taxi.city.citytaxidriver.networking.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import taxi.city.citytaxidriver.models.Car;

public class NCar {
    @Expose
    public int id;

    @Expose
    public int driver;

    @Expose
    public int brand;

    @Expose
    @SerializedName("brand_model")
    public int model;

    @Expose
    public String color;

    @Expose
    public int year;

    @Expose
    @SerializedName("car_number")
    public String number;

    public NCar() {
    }

    public NCar(Car car) {
        id = car.getId();
        driver = car.getDriverId();
        brand = car.getBrand().getBrandId();
        model = car.getModel().getBrandModelId();
        color = car.getColor();
        year = car.getYear();
        number = car.getNumber();
    }
}
