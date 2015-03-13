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
    EditText loginPassword;
    EditText loginPhone;

    // UI references.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Set up the login form.
        loginPhone = (EditText) findViewById(R.id.login_phone);
        Button btnSignIn = (Button) findViewById(R.id.btnSignIn);
        Button btnSignUp = (Button) findViewById(R.id.btnSignUp);
        loginPassword = (EditText) findViewById(R.id.login_password);

        btnSignIn.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
    }

    private void SignUpActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        EditText phone = (EditText) findViewById(R.id.login_phone);
        intent.putExtra(EXTRA_MESSAGE, phone.toString());
        startActivity(intent);
    }

    private void StartMapsActivity(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private boolean ValidateInputFields() {
        String login = String.valueOf(loginPhone.getText());
        String password = String.valueOf(loginPassword.getText());

        if (login == null || login.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Введите логин", Toast.LENGTH_LONG).show();
            return false;
        }
        if (password == null || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Введите пароль", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
//            case R.id.btnSignIn:
//                Toast.makeText(getApplicationContext(), loginPassword.toString(), Toast.LENGTH_LONG).show();
//                break;
            case R.id.btnSignIn:
                if(ValidateInputFields()) {
                   StartMapsActivity();
                }
                break;
            case R.id.btnSignUp:
                SignUpActivity();
                break;
        }
    }
}