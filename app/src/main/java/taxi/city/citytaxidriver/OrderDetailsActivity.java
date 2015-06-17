package taxi.city.citytaxidriver;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.fragments.OrderDetailsFragment;


public class OrderDetailsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_activity);

        if (User.getInstance() == null) {
            finish();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new OrderDetailsFragment())
                    .commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
