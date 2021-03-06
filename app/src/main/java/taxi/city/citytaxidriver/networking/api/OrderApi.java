package taxi.city.citytaxidriver.networking.api;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import taxi.city.citytaxidriver.db.models.Tariff;
import taxi.city.citytaxidriver.models.Order;
import taxi.city.citytaxidriver.models.OrderStatus;
import taxi.city.citytaxidriver.networking.model.BOrder;
import taxi.city.citytaxidriver.db.models.OrderModel;

public interface OrderApi {
    @GET("/info_orders/")
    void getAllByStatusAndDriver(@Query("driver") int userId, @Query("status") OrderStatus status, @Query("ordering") String sortBy, @Query("limit") int limit, Callback<ArrayList<Order>> cb);

    @GET("/orders/")
    void getAllByDistance(@Query("status") OrderStatus status, @Query("dist") double dist, Callback<ArrayList<OrderModel>> cb);

    @GET("/info_orders/?status=sos")
    void getSosOrders(Callback<ArrayList<Order>> cb);

    @GET("/info_orders/{orderId}/")
    void getById(@Path("orderId") int orderId, Callback<Order> cb);

    @POST("/orders/")
    void createOrder(@Body BOrder order, Callback<OrderModel> cb);

    @FormUrlEncoded
    @PATCH("/orders/{orderId}/")
    void updateStatus(@Path("orderId") int orderId, @Field("status") OrderStatus status, Callback<OrderModel> cb);

    @FormUrlEncoded
    @PATCH("/orders/{orderId}/")
    void cancelOrder(@Path("orderId") int orderId, @Field("status") OrderStatus status, @Field("driver") int driverId, Callback<OrderModel> cb);

    @PATCH("/orders/{orderId}/")
    void update(@Path("orderId") int orderId, @Body OrderModel order, Callback<OrderModel> cb);

    @GET("/tariffs/")
    void  getAllTariffs(Callback<List<Tariff>> cb);
}
