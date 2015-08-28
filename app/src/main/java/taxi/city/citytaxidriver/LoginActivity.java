package taxi.city.citytaxidriver;

import android.os.Bundle;

import taxi.city.citytaxidriver.fragments.LoginFragment;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, new LoginFragment())
                .commit();
    }
}
