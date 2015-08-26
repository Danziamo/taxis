package taxi.city.citytaxidriver.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import taxi.city.citytaxidriver.App;
import taxi.city.citytaxidriver.db.models.OrderModel;
import taxi.city.citytaxidriver.networking.ApiService;
import taxi.city.citytaxidriver.networking.RestClient;
import taxi.city.citytaxidriver.networking.api.OrderApi;
import taxi.city.citytaxidriver.utils.Helper;

/**
 * Created by mbt on 8/20/15.
 */
public class LocationService extends Service {

    private Context context;

    private LocationManager locationManager;
    private Criteria criteria;

    private SharedPreferences sharedPreferences;

    private Handler handler;
    private Runnable runnable;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(handler == null){
            context = getApplicationContext();
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            sharedPreferences = context.getSharedPreferences(Helper.USER_PREFS, 0);
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
//                    sendHeartBeat();
                    sendLocation();
                    sendFinishedOrders();
                    handler.postDelayed(this, 60*1000);
                }
            };

            handler.postDelayed(runnable, 0);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    public void sendHeartBeat(){
        context.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
        context.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
    }

    public void sendLocation(){
        final Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        long currentTime = System.currentTimeMillis();
        if (location != null && location.getTime() >= (currentTime - 15*60*1000))
        {
            final int userId = sharedPreferences.getInt("id", 0);
            final String token = sharedPreferences.getString("token", null);

            if(userId != 0 && token != null) {
                sendHeartBeat();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            JSONObject data = new JSONObject();
                            data.put("cur_position", Helper.getFormattedLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                            ApiService.getInstance().patchRequest(data, "users/" + userId + "/", token);
                        } catch (Exception e) {
                            //silent
                        }
                        return null;
                    }
                }.execute();
            }else{
                stopSelf();
            }
        }else if(currentTime % 5*60*1000 < 60*1000){
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {}
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override
                    public void onProviderEnabled(String provider) {}
                    @Override
                    public void onProviderDisabled(String provider) {}
                }, null);
            }
        }
    }

    private void sendFinishedOrders(){

        if( !Helper.isNetworkAvailable(App.getContext()) ){
            return;
        }

        List<OrderModel> orders = OrderModel.getAllFinishedOrders();
        int size = orders.size();

        OrderApi api = RestClient.getOrderService();

        for(int i = 0; i < size; i++){
            final OrderModel order = orders.get(i);
            api.update(order.getOrderId(), order, new Callback<OrderModel>() {
                @Override
                public void success(OrderModel orderModel, Response response) {
                    order.delete();
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
