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
import taxi.city.citytaxiclient.models.Order;
import taxi.city.citytaxiclient.models.OrderStatus;
import taxi.city.citytaxiclient.networking.model.NOrder;

public interface OrderApi {
    @GET("/info_orders/")
    void getAll(@Query("client") int userId, @Query("status") OrderStatus status, @Query("ordering") String type, @Query("limit") int limit, Callback<ArrayList<Order>> cb);

    @GET("/orders/{orderId}/")
    void getById(@Path("orderId") int orderId, Callback<Order> cb);

    @POST("/orders/")
    void createOrder(@Body NOrder order, Callback<Order> cb);

    @FormUrlEncoded
    @PATCH("/orders/{orderId}/")
    void updateStatus(@Path("orderId") int orderId, @Field("status") OrderStatus status, Callback<Order> cb);
}
