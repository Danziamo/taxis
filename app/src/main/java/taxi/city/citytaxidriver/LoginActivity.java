package taxi.city.citytaxidriver;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import taxi.city.citytaxidriver.Core.Order;
import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Service.ApiService;
import taxi.city.citytaxidriver.Utils.Helper;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String PREFS_NAME = "MyPrefsFile";
    private UserLoginTask mAuthTask = null;

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "LoginActivity";

    // UI references.
    private AutoCompleteTextView mPhoneView;
    private EditText mPasswordView;
    private View mProgressView;
    private Button mPhoneSignInButton;
    //private View mLoginFormView;
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
        mPhoneView = (AutoCompleteTextView) findViewById(R.id.login_phone);
        populateAutoComplete();

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
        mPhoneSignInButton.setEnabled(false);
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
            } else {
                mPhoneSignInButton.setEnabled(true);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        //mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
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
            mPhoneView.setText(settings.getString("phoneKey", null));
            if (settings.contains("passwordKey")) {
                mPasswordView.setText(settings.getString("passwordKey", null));
            }
        }
        if (settings.contains("deviceTokenKey")) mRegId = settings.getString("deviceTokenKey", null);
    }

    private void savePreferences(User user) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("phoneKey", mPhoneView.getText().toString());
        editor.putString("passwordKey", mPasswordView.getText().toString());
        editor.putString("tokenKey", user.getToken());
        editor.putString("deviceTokenKey", user.deviceToken);
        api.setToken(user.getToken());
        editor.apply();
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPhoneView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneView.getText().toString();
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
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mPhoneView.setAdapter(adapter);
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
                json.put("android_token", mRegId);
                JSONObject object = api.loginRequest(json, "login/");
                if (object != null) {
                    statusCode = object.getInt("status_code");
                    if (statusCode == HttpStatus.SC_OK) {
                        user.setUser(object);
                        id = object.getInt("id");
                        JSONArray cars = api.hasCar(null, "usercars/");
                        if (cars != null && cars.length() > 0) {
                            for (int i = 0; i < cars.length(); ++i) {
                                JSONObject row = cars.getJSONObject(i);
                                if (row.getInt("driver") == id) {
                                    hasCar = true;
                                    break;
                                }
                            }
                        }
                        JSONObject resultObject = api.getDataFromGetRequest("?status=accepted&driver="+user.id, "orders/");
                        driverHasOrder(resultObject);
                        resultObject = api.getDataFromGetRequest("?status=waiting&driver="+user.id, "orders/");
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
                Log.i(TAG, "This device is not supported.");
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
            protected void onPostExecute(String msg) {
                if (msg.equals("done")) mPhoneSignInButton.setEnabled(true);
                else Toast.makeText(getApplicationContext(), "Cannot fetch ID from google", Toast.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
    }
}