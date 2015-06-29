package taxi.city.citytaxidriver;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
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
import org.w3c.dom.Text;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import taxi.city.citytaxidriver.core.Order;
import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.enums.OStatus;
import taxi.city.citytaxidriver.service.ApiService;
import taxi.city.citytaxidriver.utils.Helper;

public class LoginActivity extends Activity{

    private static final String PREFS_NAME = "MyPrefsFile";
    private UserLoginTask mAuthTask = null;
    private ForgotPasswordTask mForgotTask = null;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private EditText mPhoneView;
    private TextView mPhoneExtraView;
    private EditText mPasswordView;
    private Button mPhoneSignInButton;
    private TextView mForgotPassword;

    SweetAlertDialog pDialog;
    private User user = User.getInstance();
    private int statusCode;
    private String detail;

    private ApiService api;
    private Order order = Order.getInstance();

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        api = ApiService.getInstance();

        // Set up the login form.
        mPhoneView = (EditText) findViewById(R.id.login_phone);
        mPhoneExtraView = (TextView) findViewById(R.id.textViewPhoneExtra);
        mForgotPassword = (TextView) findViewById(R.id.textViewForgotPassword);
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
        mForgotPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(LoginActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.forgot_password_alert);

                Window window = dialog.getWindow();
                WindowManager.LayoutParams wlp = window.getAttributes();

                wlp.gravity = Gravity.BOTTOM;
                wlp.dimAmount = 0.7f;
                wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                window.setAttributes(wlp);

                final TextView tvExtraPhone = (TextView) dialog.findViewById(R.id.textViewForgotPhoneExtra);
                final EditText etPhone = (EditText) dialog.findViewById(R.id.etForgotPhone);

                Button btnOkDialog = (Button) dialog.findViewById(R.id.buttonOkDecline);
                Button btnCancelDialog = (Button) dialog.findViewById(R.id.buttonCancelDecline);

                btnOkDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String forgotPhone = tvExtraPhone.getText().toString() + etPhone.getText().toString();
                        if (forgotPhone.length() != 13) {
                            etPhone.requestFocus();
                            etPhone.setError("Неправильный формат");
                        }
                        forgotPassword(forgotPhone);

                        dialog.dismiss();
                    }
                });

                btnCancelDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
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
    }

    private void forgotPassword(String phone) {
        if (mForgotTask != null) return;

        showProgress(true);
        mForgotTask = new ForgotPasswordTask(phone);
        mForgotTask.execute((Void) null);
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
    }

    private void savePreferences(User user) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("phoneKey", mPhoneExtraView.getText().toString() + mPhoneView.getText().toString());
        editor.putString("passwordKey", mPasswordView.getText().toString());
        editor.putString("tokenKey", user.getToken());
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
        return password.length() >= 4;
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
                        ApiService.getInstance().setToken(user.getToken());
                        Helper.saveUserPreferences(LoginActivity.this, user);
                        id = object.getInt("id");
                        if (object.has("cars") && object.getJSONArray("cars").length() > 0) hasCar = true;
                        JSONObject regObject = new JSONObject();
                        regObject.put("online_status", "online");
                        regObject.put("role", "driver");
                        regObject.put("ios_token", JSONObject.NULL);
                        JSONObject updateObject = api.patchRequest(regObject, "users/" + id + "/");
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
            } else  if (Helper.isBadRequest(statusCode)) {
                if (detail != null && detail.toLowerCase().contains("account")) {
                    User.getInstance().phone = mPhone;
                    User.getInstance().password = mPassword;
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
        intent.putExtra("PHONE", mPhoneExtraView.getText().toString() + mPhoneView.getText().toString());
        intent.putExtra("PASS", mPasswordView.getText().toString());
        startActivity(intent);
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

    public class ForgotPasswordTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mPhone;

        ForgotPasswordTask(String phone) {
            mPhone = phone;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            return api.resetPasswordRequest("reset_password/?phone=" + mPhone.replace("+", "%2b"));
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            mForgotTask = null;
            showProgress(false);
            try {
                if (Helper.isSuccess(result)) {
                    Intent intent = new Intent(LoginActivity.this, ConfirmSignUpActivity.class);
                    intent.putExtra("SIGNUP", false);
                    intent.putExtra("PHONE", mPhone);
                    user.phone = mPhone;
                    startActivity(intent);
                } else if (Helper.isBadRequest(result)) {
                    Toast.makeText(LoginActivity.this, "Такого номера не существует", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Сервис не доступен", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException ignored) {}
        }

        @Override
        protected void onCancelled() {
            mForgotTask = null;
            showProgress(false);
        }
    }

}