package taxi.city.citytaxidriver.networking.api;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import taxi.city.citytaxidriver.models.Brand;
import taxi.city.citytaxidriver.models.BrandModel;
import taxi.city.citytaxidriver.networking.model.NCar;

public interface CarApi {
    @GET("/usercars/{carId}/")
    void getCarById(@Path("carId") int id, Callback<NCar> cb);

    @POST("/usercars/")
    void addCar(@Body NCar car, Callback<Object> cb);

    @PATCH("/usercars/{carId}")
    void updateCar(@Body NCar car, Callback<Object> cb);

    @GET("/cars/carbrands/")
    void getAllCarBrands(Callback<ArrayList<Brand>> cb);

    @GET("/cars/carbrands/{brandId}/")
    void getCarBrandById(@Path("brandId") int id, Callback<Brand> cb);

    @GET("/cars/carbrandmodels/{modelId}/")
    void getCarModelById(@Path("modelId") int id, Callback<BrandModel> cb);

    @GET("/cars/carbrandmodels/")
    void getCarModelByBrandId(@Query("brand_id") int brandId, Callback<ArrayList<BrandModel>> cb);
}
