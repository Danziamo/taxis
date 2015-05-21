package taxi.city.citytaxidriver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import taxi.city.citytaxidriver.core.Order;
import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.service.ApiService;
import taxi.city.citytaxidriver.utils.Helper;

/**
 * Created by Daniyar on 5/20/2015.
 */
public abstract class BaseActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (User.getInstance() == null || User.getInstance().id == 0) {
            Helper.getUserPreferences(this);
            Helper.getOrderPreferences(this);
            ApiService.getInstance().setToken(User.getInstance().getToken());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Helper.saveUserPreferences(this, User.getInstance());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (User.getInstance() == null || User.getInstance().id == 0) {
            Helper.getUserPreferences(this);
            Helper.getOrderPreferences(this);
            ApiService.getInstance().setToken(User.getInstance().getToken());
        }
    }
}
