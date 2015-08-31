package taxi.city.citytaxidriver.db.models;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by mbt on 8/28/15.
 */
@Table(name = "brand_models")
public class BrandModel extends Model implements Serializable {
    @Expose
    @SerializedName("id")
    @Column(name = "brand_model_id")
    private int brandModelId;

    @Expose
    @SerializedName("brand_model_name")
    @Column(name = "brand_model_name")
    private String name;

    @Expose
    @SerializedName("car_brand")
    @Column(name = "car_brand")
    private Brand brand;

    public BrandModel() {}

    public int getBrandModelId() {
        return brandModelId;
    }

    public void setBrandModelId(int brandModelId) {
        this.brandModelId = brandModelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public static BrandModel getByBrandModelId(int brandModelId){
        return new Select()
                .from(BrandModel.class)
                .where("brand_model_id = ?", brandModelId)
                .executeSingle();
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BrandModel brandModel = (BrandModel) o;

        if (getBrandModelId() != brandModel.getBrandModelId()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getBrandModelId();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
