package taxi.city.citytaxidriver.utils;


import android.content.Context;
import android.content.SharedPreferences;

import taxi.city.citytaxidriver.App;
import taxi.city.citytaxidriver.core.User;

/**
 * Created by mbt on 7/23/15.
 */
public class SessionHelper {

    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String PHONE_KEY = "phoneKey";
    public static final String PASSWORD_KEY = "passwordKey";
    public static final String ID_KEY = "idKey";
    public static final String TOKEN_KEY = "tokenKey";

    private String phone;
    private String password;
    private int id;
    private String token;

    private Context context;
    private SharedPreferences sharedPreferences;

    public SessionHelper(){
        context = App.getContext();
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, 0);

        phone = "";
        password = "";
        id = 0;
        token = "";


        if(sharedPreferences.contains(PHONE_KEY)){
            phone = sharedPreferences.getString(PHONE_KEY, "");
        }

        if(sharedPreferences.contains(PASSWORD_KEY)){
            password = sharedPreferences.getString(PASSWORD_KEY, "");
        }

        if(sharedPreferences.contains(ID_KEY)){
            id = sharedPreferences.getInt(ID_KEY, 0);
        }

        if (sharedPreferences.contains(TOKEN_KEY)){
            token = sharedPreferences.getString(TOKEN_KEY, "");
        }
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        putValue(PHONE_KEY, phone);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        putValue(PASSWORD_KEY, password);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        putValue(ID_KEY, id);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        putValue(TOKEN_KEY, token);
    }

    private void putValue(String key, String value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void putValue(String key, int value){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void save(User user){
        setPhone(user.phone);
        setPassword(user.password);
        setId(user.id);
        setToken(user.getToken());
    }
}
