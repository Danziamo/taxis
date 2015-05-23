package taxi.city.citytaxidriver;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.service.ApiService;
import taxi.city.citytaxidriver.utils.Helper;


public class ConfirmSignUpActivity extends BaseActivity {
    private EditText mActivationCode;
    private ActivateTask task = null;
    SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_sign_up);
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
        if (show) {
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper()
                    .setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Активация");
            pDialog.setCancelable(true);
            pDialog.show();
        } else {
            pDialog.dismissWithAnimation();
        }
    }

    private class ActivateTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mCode;
        private JSONObject json = new JSONObject();

        ActivateTask(String code) {
            mCode = code;

            try {
                json.put("phone", User.getInstance().phone);
                json.put("password", User.getInstance().password);
                json.put("activation_code", mCode);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            return ApiService.getInstance().activateRequest(json, "activate/");
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            task = null;
            showProgress(false);
            try {
                if (Helper.isSuccess(result)) {
                    Finish();
                } else {
                    Toast.makeText(ConfirmSignUpActivity.this, "Не удалось активировать пользователя", Toast.LENGTH_LONG).show();
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
