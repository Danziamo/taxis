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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.service.ApiService;
import taxi.city.citytaxidriver.utils.Helper;


public class ConfirmSignUpActivity extends BaseActivity {
    private EditText mActivationCode;
    private EditText mPasswordField;
    private ActivateTask task = null;
    private String mPhone;
    SweetAlertDialog pDialog;
    private boolean isSignUp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_sign_up);
        Intent intent = getIntent();
        isSignUp = intent.getBooleanExtra("SIGNUP", true);
        Initialize();
    }

    private void Initialize() {
        mActivationCode = (EditText) findViewById(R.id.etActivationCode);
        mPasswordField = (EditText) findViewById(R.id.etActivationPassword);
        TextView tvTitle = (TextView) findViewById(R.id.textViewTitle);
        Button btn = (Button) findViewById(R.id.btnActivate);

        if (isSignUp) {
            mPasswordField.setVisibility(View.INVISIBLE);
        } else {
            tvTitle.setText("Восстановление пароля");
            btn.setText("Подтвердить");
        }
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
        String password = mPasswordField.getText().toString();
        View focusView = null;
        boolean cancel = false;

        if (mCode == null || mCode.length() > 5) {
            mActivationCode.setError("Код не более 5 символов");
            focusView = mActivationCode;
            cancel = true;
        }

        if (!isSignUp && password.length() < 4) {
            mPasswordField.setError("Минимум 4 символа");
            focusView = mPasswordField;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            task = new ActivateTask(mCode, password);
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
        private final String mPassword;
        private JSONObject json = new JSONObject();

        ActivateTask(String code, String password) {
            mCode = code;
            mPassword = password;

            try {
                json.put("phone", User.getInstance().phone);
                json.put("password", isSignUp ? User.getInstance().password : mPassword);
                json.put("activation_code", mCode);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            if (isSignUp) {
                return ApiService.getInstance().activateRequest(json, "activate/");
            } else {
                String uri = "?phone=" + User.getInstance().phone
                        + "&password=" + (mPassword)
                        + "&activation_code=" + mCode;
                return ApiService.getInstance().putRequest(null, "reset_password/" + uri.replace("+", "%2b"));
            }
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            task = null;
            showProgress(false);
            try {
                if (Helper.isSuccess(result) && result.has("detail") && result.getString("detail").toLowerCase().equals("ok")) {
                    User.getInstance().setUser(result);
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
        /*if (isSignUp) {
            Intent intent = new Intent(this, CarDetailsActivity.class);
            intent.putExtra("NEW", true);
            startActivity(intent);
        }*/
        new SweetAlertDialog(ConfirmSignUpActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Успешно")
                .setContentText(null)
                .setConfirmText("Ок")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        finish();
                    }
                })
                .show();
    }
}
