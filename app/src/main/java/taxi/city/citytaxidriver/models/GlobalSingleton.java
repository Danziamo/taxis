package taxi.city.citytaxidriver.models;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import taxi.city.citytaxidriver.App;

public class GlobalSingleton {
    Context context;
    private static GlobalSingleton instance;

    public User currentUser;
    public Order currentOrder;
    public String token;
    public LatLng curPosition;
    public ArrayList<Order> newOrders;

    public static GlobalSingleton getInstance(Context context) {
        if(instance == null) instance = new GlobalSingleton(context);
        if(instance.context == null) instance.context = context;
        return instance;
    }

    public static GlobalSingleton getInstance(){
        return getInstance(App.getContext());
    }

    private GlobalSingleton(Context context) {
        this.context = context;
    }

    public String getPosition() {
        if (curPosition == null) return null;
        return "POINT (" + curPosition.latitude + " " + curPosition.longitude + ")";
    }
}
