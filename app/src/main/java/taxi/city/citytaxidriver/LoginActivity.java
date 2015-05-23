package taxi.city.citytaxidriver;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class LoginActivity extends Activity{

    private static final String PREFS_NAME = "MyPrefsFile";
    private UserLoginTask mAuthTask = null;

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "LoginActivity";

    private EditText mPhoneView;
    private TextView mPhoneExtraView;
    private EditText mPasswordView;
    private Button mPhoneSignInButton;

    SweetAlertDialog pDialog;
    private User user = User.getInstance();
    private int statusCode;
    private String detail;

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

        mPasswordView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPasswordView.setError(null);
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
        editor.putString("passwordKey", mPasswordView.getText().toString());
        editor.putString("tokenKey", user.getToken());
        editor.putString("deviceTokenKey", user.deviceToken);
        api.setToken(user.getToken());
        editor.apply();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        if (!Helper.isNetworkAvailable(LoginActivity.this)) {
            Toast.makeText(this, "Нету подключения к интернету", Toast.LENGTH_SHORT).show();
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
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(phone, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPhoneValid(String phone) {
        return true;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

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
                    if (object.has("detail")) detail = object.getString("detail");
                    if (statusCode == HttpStatus.SC_OK) {
                        user.setUser(object);
                        Helper.saveUserPreferences(LoginActivity.this, user);
                        id = object.getInt("id");
                        JSONObject cars = api.getArrayRequest(null, "usercars/?driver=" + user.id);
                        if (Helper.isSuccess(cars) && cars.has("result") && cars.getJSONArray("result").length() > 0) hasCar = true;
                        if (mRegId != null) {
                            JSONObject regObject = new JSONObject();
                            regObject.put("online_status", "online");
                            regObject.put("android_token", mRegId);
                            JSONObject updateObject = api.patchRequest(regObject, "users/" + id + "/");
                        }
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
                if (detail != null && detail.contains("Account")) {
                    goToActivation();
                } else {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                }
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

    private void goToActivation() {
        Intent intent = new Intent(LoginActivity.this, ConfirmSignUpActivity.class);
        startActivity(intent);
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

    private void NextActivity(boolean hasCar) {
        savePreferences(user);
        Helper.saveUserPreferences(LoginActivity.this, user);
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