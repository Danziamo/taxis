package taxi.city.citytaxidriver.models;

import android.content.Context;

public class GlobalSingleton {
    Context context;
    private static GlobalSingleton instance;

    public User currentUser;
    public Order currentOrder;
    public String token;


    public static GlobalSingleton getInstance(Context context) {
        if(instance == null) instance = new GlobalSingleton(context);
        if(instance.context == null) instance.context = context;
        return instance;
    }

    private GlobalSingleton(Context context) {
        this.context = context;
    }
}
