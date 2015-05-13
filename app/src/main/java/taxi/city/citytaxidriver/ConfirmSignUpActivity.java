package taxi.city.citytaxidriver;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import taxi.city.citytaxidriver.service.ApiService;


public class ConfirmSignUpActivity extends Activity {

    private View mProgressView;
    //private View mLoginFormView;
    private EditText mActivationCode;
    private ActivateTask task = null;
    JSONObject mUserObject = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_sign_up);
        Intent intent = getIntent();
        try {
            mUserObject =  new JSONObject(intent.getStringExtra("DATA"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Initialize();
    }

    private void Initialize() {
        mActivationCode = (EditText) findViewById(R.id.etActivationCode);

        Button btn = (Button) findViewById(R.id.btnActivate);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activate();
            }
        });

        //mLoginFormView = findViewById(R.id.svActivationCode);
        mProgressView = findViewById(R.id.activate_progress);
    }

    private void activate() {
        if (task != null) {
            return;
        }

        String mCode = mActivationCode.getText().toString();
        View focusView = null;
        boolean cancel = false;

        if (mCode == null || mCode.length() != 5) {
            mActivationCode.setError("Код должен состоять из 5 символов");
            focusView = mActivationCode;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            task = new ActivateTask(mCode);
            task.execute((Void) null);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            /*mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });*/

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
            //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private class ActivateTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mCode;
        private JSONObject json = new JSONObject();
        private int id;

        ActivateTask(String code) {
            mCode = code;
            json = mUserObject;

            try {
                id = json.getInt("id");
                json.put("activation_code", mCode);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.
            if (json.has("status_code")) {
                json.remove("status_code");
            }
            return ApiService.getInstance().activateRequest(json, "users/" + id + "/");
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            task = null;
            showProgress(false);
            try {
                int statusCode = result.getInt("status_code");

                if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_ACCEPTED || statusCode == HttpStatus.SC_CREATED) {
                    Finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Сервис недоступен", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            task = null;
            showProgress(false);
        }
    }

    private void Finish() {
        Intent intent = new Intent(this, CarDetailsActivity.class);
        intent.putExtra("NEW", true);
        startActivity(intent);
        finish();
    }
}
