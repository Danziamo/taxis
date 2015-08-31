package taxi.city.citytaxidriver.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import taxi.city.citytaxidriver.db.models.Brand;
import taxi.city.citytaxidriver.db.models.BrandModel;
import taxi.city.citytaxidriver.networking.model.NCar;

public class Car implements Serializable {

    @Expose
    private int id;

    @Expose
    @SerializedName("driver")
    private int driverId;

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

    public Car() {
    }

    public Car(NCar nCar){
        id = nCar.id;
        driverId = nCar.driver;
        Brand brand = Brand.getByBrandId(nCar.brand);
        if(brand == null){
            brand = new Brand();
            brand.setBrandId(nCar.brand);
        }
        this.brand = brand;

        BrandModel brandModel = BrandModel.getByBrandModelId(nCar.model);
        if(brandModel == null){
            brandModel = new BrandModel();
            brandModel.setBrandModelId(nCar.model);
        }
        this.model = brandModel;

        this.color = nCar.color;
        this.year = nCar.year;
        this.number = nCar.number;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

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
