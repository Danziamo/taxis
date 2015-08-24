package taxi.city.citytaxidriver.networking.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
    @SerializedName("car_number")
    public String number;

    @Expose
    @SerializedName("technical_certificate")
    public String technicalCertificate;
}
