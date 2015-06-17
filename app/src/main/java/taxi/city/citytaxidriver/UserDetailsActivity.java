package taxi.city.citytaxidriver;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.fragments.UserDetailsFragment;
import taxi.city.citytaxidriver.utils.Helper;


public class UserDetailsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new UserDetailsFragment())
                    .commit();
        }
    }

    /*@Override
    protected void onPause() {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(noteTv.getWindowToken(), 0);
        super.onPause();
    }*/

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
