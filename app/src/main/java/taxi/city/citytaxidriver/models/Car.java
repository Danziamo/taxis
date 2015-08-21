package taxi.city.citytaxidriver.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Car implements Serializable {

    @Expose
    private Brand brand;

    @Expose
    @SerializedName("brand_model")
    private BrandModel model;

    @Expose
    private String color;

    @Expose
    private int year;

    @Expose
    @SerializedName("car_number")
    private String number;

    @Expose
    private String photo;

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public BrandModel getModel() {
        return model;
    }

    public void setModel(BrandModel model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
