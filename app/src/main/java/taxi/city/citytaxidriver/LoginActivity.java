package taxi.city.citytaxidriver;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import taxi.city.citytaxidriver.core.Order;
import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.service.ApiService;
import taxi.city.citytaxidriver.utils.Helper;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity{

    private static final String PREFS_NAME = "MyPrefsFile";
    private UserLoginTask mAuthTask = null;

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "LoginActivity";

    // UI references.
    private EditText mPhoneView;
    private TextView mPhoneExtraView;
    private EditText mPasswordView;
    //private View mProgressView;
    private Button mPhoneSignInButton;
    //private View mLoginFormView;

    SweetAlertDialog pDialog;
    private User user = User.getInstance();
    private int statusCode;

    private ApiService api = ApiService.getInstance();
    private Order order = Order.getInstance();

    private SharedPreferences settings;
    String SENDER_ID = "400358386973";
    String mRegId;
    GoogleCloudMessaging gcm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Set up the login form.
        mPhoneView = (EditText) findViewById(R.id.login_phone);
        mPhoneExtraView = (TextView) findViewById(R.id.textViewPhoneExtra);
        mPasswordView = (EditText) findViewById(R.id.login_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login_phone || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });


        mPhoneSignInButton = (Button) findViewById(R.id.btnSignIn);
        mPhoneSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mPhoneSignUpButton = (Button) findViewById(R.id.btnSignUp);
        mPhoneSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        setPreferences();

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);

            if (mRegId == null || mRegId.length() < 10 || mRegId.isEmpty()) {
                registerInBackground();
            }
        }
    }

    private void signUp() {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra("NEW", true);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (data != null && data.getExtras() != null)
                Toast.makeText(getApplicationContext(), data.getExtras().getString("MESSAGE"), Toast.LENGTH_LONG).show();
        }
    }

    private void setPreferences() {
        settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.contains("phoneKey")) {
            String phone = settings.getString("phoneKey", null);
            if (phone != null && phone.length() > 5) {
                mPhoneView.setText(phone.substring(4, phone.length()));
                if (settings.contains("passwordKey")) {
                    mPasswordView.setText(settings.getString("passwordKey", null));
                }
            }
        }
        if (settings.contains("deviceTokenKey")) mRegId = settings.getString("deviceTokenKey", null);
    }

    private void savePreferences(User user) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("phoneKey", mPhoneExtraView.getText().toString() + mPhoneView.getText().toString());
        editor.putString("phoneKey", mPhoneView.getText().toString());
        editor.putString("passwordKey", mPasswordView.getText().toString());
        editor.putString("tokenKey", user.getToken());
        editor.putString("deviceTokenKey", user.deviceToken);
        api.setToken(user.getToken());
        editor.apply();
    }

    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPhoneView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneExtraView.getText().toString() + mPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            mPhoneView.setError(getString(R.string.error_invalid_phone));
            focusView = mPhoneView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(phone, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPhoneValid(String phone) {
        //TODO: Replace this with your own logic
        //return email.contains("@");
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        if (show) {
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper()
                    .setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Авторизация");
            pDialog.setCancelable(true);
            pDialog.show();
        } else {
            pDialog.dismissWithAnimation();
        }
    }

    private void driverHasOrder(JSONObject object) {
        try {
            if (object == null)
                return;
            if (!object.has("status_code") || object.getInt("status_code") != HttpStatus.SC_OK)
                return;
            if (!object.has("result") || object.getJSONArray("result").length() < 1)
                return;
            JSONObject row = object.getJSONArray("result").getJSONObject(0);
            Helper.setOrder(row);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mPhone;
        private final String mPassword;
        private boolean hasCar = false;
        private int id = 0;

        UserLoginTask(String phone, String password) {
            mPhone = phone;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.
            boolean res = false;
            try {
                JSONObject json = new JSONObject();
                user.phone = mPhone;
                user.password = mPassword;
                json.put("phone", mPhone);
                json.put("password", mPassword);
                JSONObject object = api.loginRequest(json, "login/");
                if (object != null) {
                    statusCode = object.getInt("status_code");
                    if (statusCode == HttpStatus.SC_OK) {
                        user.setUser(object);
                        id = object.getInt("id");
                        JSONObject cars = api.getDataFromGetRequest(null, "usercars/?driver=" + user.id);
                        if (Helper.isSuccess(cars) && cars.has("result") && cars.getJSONArray("result").length() > 0) hasCar = true;
                        if (mRegId != null) {
                            JSONObject regObject = new JSONObject();
                            regObject.put("online_status", true);
                            regObject.put("android_token", mRegId);
                            JSONObject updateObject = api.patchRequest(regObject, "users/" + id + "/");
                        }
                        JSONObject resultObject = api.getDataFromGetRequest("?status=accepted&driver="+user.id, "orders/");
                        driverHasOrder(resultObject);
                        resultObject = api.getDataFromGetRequest("?status=pending&driver="+user.id, "orders/");
                        driverHasOrder(resultObject);
                        resultObject = api.getDataFromGetRequest("?status=ontheway&driver="+user.id, "orders/");
                        driverHasOrder(resultObject);
                        res = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                res = false;
            }
            return res;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            if (success && statusCode == 200) {
                NextActivity(hasCar);
            } else  if (statusCode == 403) {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
            else {
                Toast.makeText(getApplicationContext(), "Сервис недоступен", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void NextActivity(boolean hasCar) {
        savePreferences(user);
        Intent intent;
        if (hasCar) {
            intent = new Intent(LoginActivity.this, MapsActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, CarDetailsActivity.class);
        }
        intent.putExtra("NEW", true);
        startActivity(intent);
        finish();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    mRegId = gcm.register(SENDER_ID);
                    msg = "done";

                } catch (IOException ex) {
                    msg = "err";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {}
        }.execute(null, null, null);
    }
}