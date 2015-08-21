package taxi.city.citytaxidriver.nerworking.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Query;
import taxi.city.citytaxidriver.models.User;
import taxi.city.citytaxidriver.nerworking.model.AccountActivation;

public interface AccountRestoreApi {

    @GET("/reset_password/")
    void forgotPasswordRequest(@Query("phone") String phone, Callback<Object> cb);

    @PUT("/reset_password/")
    void updateForgotPassword(@Query("phone") String phone, @Query("password") String password, @Query("activation_code") String code, Callback<Object> cb);

    @POST("/activate/")
    void activate(@Body AccountActivation accountActivation, Callback<User> cb);
}
