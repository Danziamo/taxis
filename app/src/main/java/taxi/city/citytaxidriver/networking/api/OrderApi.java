package taxi.city.citytaxidriver.networking.api;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import taxi.city.citytaxidriver.models.Order;
import taxi.city.citytaxidriver.models.OrderStatus;
import taxi.city.citytaxidriver.networking.model.NOrder;

public interface OrderApi {
    @GET("/info_orders/")
    void getAllByStatusAndDriver(@Query("driver") int userId, @Query("status") OrderStatus status, @Query("ordering") String type, @Query("limit") int limit, Callback<ArrayList<Order>> cb);

    @GET("/info_orders/")
    void getAllByDistance(@Query("status") OrderStatus status, @Query("dist") int dist, Callback<ArrayList<Order>> cb);

    @GET("/info_orders/")
    void getAllByStatus(@Query("status") OrderStatus status, Callback<ArrayList<Order>> cb);

    @GET("/info_orders/{orderId}/")
    void getById(@Path("orderId") int orderId, Callback<Order> cb);

    @POST("/orders/")
    void createOrder(@Body NOrder order, Callback<Order> cb);

    @FormUrlEncoded
    @PATCH("/orders/{orderId}/")
    void updateStatus(@Path("orderId") int orderId, @Field("status") OrderStatus status, Callback<Order> cb);

    @PATCH("/orders/{orderId}/")
    void update(@Path("orderId") int orderId, @Body NOrder order, Callback<Order> cb);
}
