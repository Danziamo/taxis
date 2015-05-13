package taxi.city.citytaxidriver;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import taxi.city.citytaxidriver.fragments.FinishOrderDetailsFragment;


public class FinishOrderDetailsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_order_details);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new FinishOrderDetailsFragment())
                    .commit();
        }
    }

}
