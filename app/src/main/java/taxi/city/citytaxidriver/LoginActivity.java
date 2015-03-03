package taxi.city.citytaxidriver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements View.OnClickListener{

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    private static final String EXTRA_MESSAGE = "taxi.city.citytaxidriver.MESSAGE";

    // UI references.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Set up the login form.
        EditText loginPhone = (EditText) findViewById(R.id.login_phone);
        Button btnSignIn = (Button) findViewById(R.id.btnSignIn);
        Button btnSignUp = (Button) findViewById(R.id.btnSignUp);

        btnSignIn.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
    }

    private void SignUpActivity() {
        Log.d("Diesel", "Tamasha");
        Intent intent = new Intent(this, RegisterActivity.class);
        EditText phone = (EditText) findViewById(R.id.login_phone);
        intent.putExtra(EXTRA_MESSAGE, phone.toString());
        Log.d("Diesel", "Tamasha2");
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnSignUp:
                Toast.makeText(getApplicationContext(), "Tamasha", Toast.LENGTH_LONG).show();
                break;
            case R.id.btnSignIn:
                SignUpActivity();
                break;
        }
    }
}



