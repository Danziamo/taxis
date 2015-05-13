package taxi.city.citytaxidriver;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import taxi.city.citytaxidriver.fragments.AccountFragment;


public class AccountActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new AccountFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */

}
