package taxi.city.citytaxidriver.networking.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Query;
import taxi.city.citytaxidriver.models.User;
import taxi.city.citytaxidriver.networking.model.AccountActivation;

public interface AccountRestoreApi {

    @GET("/reset_password/")
    void forgotPasswordRequest(@Query("phone") String phone, Callback<Object> cb);

    @PUT("/reset_password/")
    void updateForgotPassword(@Query("phone") String phone, @Query("password") String password, @Query("activation_code") String code, @Body Object object, Callback<Object> cb);

    @FormUrlEncoded
    @POST("/activate/")
    void activate(@Field("phone") String phone, @Field("password") String password, @Field("activation_code") String code, Callback<User> cb);
}
