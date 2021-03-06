package taxi.city.citytaxidriver.networking.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import taxi.city.citytaxidriver.models.Session;
import taxi.city.citytaxidriver.models.User;

public interface SessionApi {
    @POST("/login/")
    void login(@Body Session session, Callback<User> callback);

    @POST("/logout/")
    void logout(@Body Object object, Callback<Object> callback);
}
