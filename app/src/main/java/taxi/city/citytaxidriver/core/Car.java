package taxi.city.citytaxidriver.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Car implements Serializable{
    public int id;
    public int brandId;
    public int modelId;
    public String brandName;
    public String modelName;
    public String color;
    public String year;
    public String technicalCertificate;
    public String number;

    public Car() {}

    public Car (JSONObject json) throws JSONException {
        this.id = json.getInt("id");
        this.brandId = json.getJSONObject("brand").getInt("id");
        this.brandName = json.getJSONObject("brand").getString("brand_name");
        this.modelId = json.getJSONObject("brand_model").getInt("id");
        this.modelName = json.getJSONObject("brand_model").getString("brand_model_name");
        this.color = json.getString("color");
        this.year = json.getString("year");
        this.number = json.getString("car_number");
        this.technicalCertificate = json.getString("technical_certificate");
    }
}
