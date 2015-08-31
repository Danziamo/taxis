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
import java.util.List;

/**
 * Created by mbt on 8/28/15.
 */
@Table(name="brands")
public class Brand extends Model implements Serializable {
    @Expose
    @SerializedName("id")
    @Column(name = "brand_id")
    private int brandId;

    @Expose
    @SerializedName("brand_name")
    @Column(name = "brand_name")
    private String name;

    public Brand(){}

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //@TODO maybe need use List everywhere
    public ArrayList<BrandModel> getBrandModels(){
        return (ArrayList)getMany(BrandModel.class, "car_brand");
    }

    public void deleteBrandModels(){
        ArrayList<BrandModel> models = getBrandModels();
        int size = models.size();
        for (int i = 0; i < size; i++){
            models.get(i).delete();
        }
    }

    public boolean isBrandModelsUpToDate(){
        long lastUpdateTime = Setting.getLongValue(Setting.BRAND_MODELS_LAST_UPDATE_TIME_NAME_PREFIX + this.getBrandId());
        return (lastUpdateTime > (System.currentTimeMillis() - 24*60*60*1000));
    }

    public void upgradeBrandModels(ArrayList<BrandModel> models){
        ArrayList<BrandModel> currentModels = getBrandModels();
        ActiveAndroid.beginTransaction();
        try{
            int size = currentModels.size();
            BrandModel model;
            for(int i = 0; i< size; i++){
                model = currentModels.get(i);
                model.delete();
            }

            size = models.size();
            for(int i = 0; i < size; i++){
                model = models.get(i);
                model.setBrand(this);
                model.save();
            }
            Setting.saveValue(Setting.BRAND_MODELS_LAST_UPDATE_TIME_NAME_PREFIX + brandId, System.currentTimeMillis());
            ActiveAndroid.setTransactionSuccessful();
        }finally {
            ActiveAndroid.endTransaction();
        }
    }





    public static boolean isBrandsUpToDate(){
        long lastUpdateTime = Setting.getLongValue(Setting.BRANDS_LAST_UPDATE_TIME_NAME);
        return (lastUpdateTime > (System.currentTimeMillis() - 24*60*60*1000));
    }

    public static ArrayList<Brand> getAll(){
        return (ArrayList)new Select()
                .from(Brand.class)
                .orderBy("brand_name")
                .execute();
    }

    public static Brand getByBrandId(int brandId){
        return new Select()
                .from(Brand.class)
                .where("brand_id = ?", brandId)
                .executeSingle();
    }

    public static void upgradeBrands(ArrayList<Brand> newBrands){
        ArrayList<Brand> currentBrands = getAll();
        ActiveAndroid.beginTransaction();
        try{
            int size = currentBrands.size();
            Brand brand;
            for(int i = 0; i < size; i++){
                brand = currentBrands.get(i);
                brand.deleteBrandModels();
                brand.delete();
            }

            size = newBrands.size();
            for(int i = 0; i < size; i++){
                brand = newBrands.get(i);
                brand.save();
            }
            Setting.saveValue(Setting.BRANDS_LAST_UPDATE_TIME_NAME, System.currentTimeMillis());
            ActiveAndroid.setTransactionSuccessful();
        }finally {
            ActiveAndroid.endTransaction();
        }
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Brand brand = (Brand) o;

        if (getBrandId() != brand.getBrandId()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getBrandId();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }


}
