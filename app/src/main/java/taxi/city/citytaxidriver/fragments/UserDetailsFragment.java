package taxi.city.citytaxidriver.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import taxi.city.citytaxidriver.App;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.models.GlobalSingleton;
import taxi.city.citytaxidriver.models.User;
import taxi.city.citytaxidriver.networking.RestClient;
import taxi.city.citytaxidriver.networking.model.NUser;
import taxi.city.citytaxidriver.utils.SessionHelper;

public class UserDetailsFragment extends BaseFragment implements View.OnClickListener {

    private EditText etLastName;
    private EditText etFirstName;
    private EditText etPassword;
    private EditText etCode;
    private TextView tvChangePassword;
    private LinearLayout llChangePassword;

    private boolean isNew = false;
    private boolean isChangePasswordClicked = false;

    private User mUser;


    public static UserDetailsFragment newInstance() {
        return new UserDetailsFragment();
    }

    public UserDetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_details, container, false);
        isNew = getActivity().getIntent().getBooleanExtra("NEW", false);

        mUser = GlobalSingleton.getInstance().currentUser;

        etLastName = (EditText)rootView.findViewById(R.id.metLastName);
        etFirstName = (EditText)rootView.findViewById(R.id.metFirstName);
        etPassword = (EditText) rootView.findViewById(R.id.metPassword);
        etCode = (EditText) rootView.findViewById(R.id.metCode);

        tvChangePassword = (TextView) rootView.findViewById(R.id.tvChangePassword);
        llChangePassword = (LinearLayout) rootView.findViewById(R.id.llChangePassword);

        tvChangePassword.setOnClickListener(this);

        ImageButton btnShowPassword = (ImageButton)rootView.findViewById(R.id.imageButtonShowPassword);

        btnShowPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    etPassword.setSelection(etPassword.length());
                    return true;
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    etPassword.setSelection(etPassword.length());
                    return true;
                }

                return false;
            }
        });

        if (!isNew) {
            etLastName.setText(mUser.getLastName());
            etFirstName.setText(mUser.getFirstName());
        }


        Button btnSave = (Button)rootView.findViewById(R.id.buttonSave);

        btnSave.setOnClickListener(this);

        rootView.findViewById(R.id.userContainer).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
        return rootView;

    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSave:
                updateUser();
                break;
            case R.id.tvChangePassword:
                sendActivationCode();
                break;
            default:
                getActivity().finish();
                break;
        }
    }

    private void updateUser(){
        if(isNew){
            App.getDefaultTracker().send(new HitBuilders.EventBuilder()
                    .setCategory("signup")
                    .setAction("signup")
                    .setLabel("Signup button pressed")
                    .build());
        }

        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();
        final String password = etPassword.getText().toString();
        final String code = etCode.getText().toString();

        if (lastName.length() < 2) {
            etLastName.setError("Пожалуйста, заполните это поле");
            etLastName.requestFocus();
            createSignupErrorAnalyticsError("Фамилия неправильно задано");
            return;
        }
        if (firstName.length() < 2) {
            etFirstName.setError("Пожалуйста, заполните это поле");
            etFirstName.requestFocus();
            createSignupErrorAnalyticsError("Имя неправильно задано");
            return;
        }

        if (isChangePasswordClicked && (password.isEmpty() || password.length() < 4)) {
            String errorStr = getString(R.string.error_invalid_password);
            etPassword.setError(errorStr);
            etPassword.requestFocus();
            createSignupErrorAnalyticsError(errorStr);
            return;
        }

        if(isChangePasswordClicked && code.length() < 4){
            String errorStr = getString(R.string.error_invalid_activation_code);
            etCode.setError(errorStr);
            etCode.requestFocus();
            createSignupErrorAnalyticsError(errorStr);
            return;
        }

        mUser.setFirstName(firstName);
        mUser.setLastName(lastName);

        showProgress("Сохранение");

        RestClient.getUserService().save(mUser.getId(), new NUser(mUser), new Callback<User>() {
            @Override
            public void success(User user, Response response) {

                GlobalSingleton.getInstance().currentUser = mUser;
                if(isChangePasswordClicked) {
                    changeUserPassword(password, code);
                }else {
                    hideProgress();
                    Toast.makeText(getActivity(), "Сохранено", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                if (error.getKind() == RetrofitError.Kind.HTTP) {
                    Toast.makeText(getActivity(), getString(R.string.error_an_error_has_occurred_try_again), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(), getString(R.string.error_could_not_connect_to_server), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void sendActivationCode(){
        showProgress(getString(R.string.wait_please));
        RestClient.getAccountService().forgotPasswordRequest(mUser.getPhone(), new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                hideProgress();
                isChangePasswordClicked = true;
                llChangePassword.setVisibility(View.VISIBLE);
                tvChangePassword.setVisibility(View.GONE);
                etFirstName.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                Toast.makeText(getActivity(), getString(R.string.sms_send_toast, mUser.getPhone()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                if (error.getKind() == RetrofitError.Kind.HTTP) {
                    Toast.makeText(getActivity(), getString(R.string.error_an_error_has_occurred_try_again), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_could_not_connect_to_server), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void changeUserPassword(final String password, String code){

        RestClient.getAccountService().updateForgotPassword(mUser.getPhone(), password, code, new Object(), new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                hideProgress();
                SessionHelper sessionHelper = new SessionHelper();
                sessionHelper.setPassword(password);
                Toast.makeText(getActivity(), "Сохранено", Toast.LENGTH_LONG).show();
                llChangePassword.setVisibility(View.GONE);
                tvChangePassword.setVisibility(View.VISIBLE);
                etFirstName.setImeOptions(EditorInfo.IME_ACTION_DONE);
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                String message = getString(R.string.error_could_not_connect_to_server);
                if (error.getKind() == RetrofitError.Kind.HTTP) {
                    String result = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                    try {
                        JSONObject json = new JSONObject(result);
                        String detail = "";
                        if(json.has("detail")){
                            detail = json.getString("detail");
                            if(detail.equals("Invalid activation code")){
                                message = getString(R.string.error_invalid_activation_code);
                            }
                        }
                    } catch (JSONException e) {
                        Crashlytics.logException(e);
                        message = getString(R.string.error_an_error_has_occurred_try_again);
                    }
                }

                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void createSignupErrorAnalyticsError(String msg){
        if(isNew) {
            App.getDefaultTracker().send(new HitBuilders.EventBuilder()
                    .setCategory("signup")
                    .setAction("signup error")
                    .setLabel("Signup Error: " + msg)
                    .build());
        }
    }


}