package taxi.city.citytaxidriver.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import taxi.city.citytaxidriver.CarDetailsActivity;
import taxi.city.citytaxidriver.LoginActivity;
import taxi.city.citytaxidriver.MainActivity;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.models.GlobalSingleton;
import taxi.city.citytaxidriver.models.User;
import taxi.city.citytaxidriver.networking.RestClient;
import taxi.city.citytaxidriver.utils.SessionHelper;


public class AuthorizationFragment extends BaseFragment implements View.OnClickListener {

    private static final String ARG_PHONE = "PHONE_KEY";
    public static final String ARG_FROM_FORGET_PASSWORD = "FROM_FORGET_PASSWORD_KEY";
    public static final String ARG_PASSWORD = "PASSWORD_KEY";

    private String phone;
    private boolean isFromForgetPassword = false;
    private String password;

    private MaterialEditText metPassword;
    private MaterialEditText metSmscode;


    public static AuthorizationFragment newInstance(String phone, String password, boolean isFromForgetPassword){
        AuthorizationFragment fragment = new AuthorizationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE, phone);
        args.putBoolean(ARG_FROM_FORGET_PASSWORD, isFromForgetPassword);
        args.putString(ARG_PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle args = getArguments();
            phone = args.getString(ARG_PHONE);
            isFromForgetPassword = args.getBoolean(ARG_FROM_FORGET_PASSWORD, false);
            password = args.getString(ARG_PASSWORD, null);
        }
    }

    public AuthorizationFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_authorization, container, false);
        metPassword = (MaterialEditText)view.findViewById(R.id.metPassword);
        metSmscode = (MaterialEditText)view.findViewById(R.id.metCode);

        metPassword.setText(password);


        Button btnActivate = (Button)view.findViewById(R.id.btnActivate);
        if(isFromForgetPassword){
            btnActivate.setText(getString(R.string.signup_submit));
            metPassword.setVisibility(View.VISIBLE);

            LinearLayout llPassword = (LinearLayout) view.findViewById(R.id.llPassword);
            llPassword.setVisibility(View.VISIBLE);

            TextView tvPassword = (TextView) view.findViewById(R.id.tvPassword);
            tvPassword.setText(getString(R.string.new_password));
        }else{
            metPassword.setFocusable(false);
            metSmscode.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
        btnActivate.setOnClickListener(this);

        TextView btnResendSms = (TextView) view.findViewById(R.id.btnResendSms);
        btnResendSms.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.btnActivate){
            if(isFromForgetPassword){
                updatePassword();
            }else {
                activate();
            }
        }else if(id == R.id.btnResendSms){
            resendSmsRequest();
        }
    }

    private void resendSmsRequest() {
        showProgress(getString(R.string.wait_please));
        RestClient.getAccountService().forgotPasswordRequest(phone, new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                hideProgress();
                Toast.makeText(getActivity(), getString(R.string.sms_sent), Toast.LENGTH_SHORT).show();
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

    private void updatePassword(){
        String newPassword = metPassword.getText().toString();
        String smsCode = metSmscode.getText().toString();

        if(smsCode.length() < 4){
            metSmscode.setError(getString(R.string.error_invalid_activation_code));
            metSmscode.requestFocus();
            return;
        }

        if(newPassword.length() < 4){
            metPassword.setError(getString(R.string.error_invalid_password));
            metPassword.requestFocus();
            return;
        }

        showProgress(getString(R.string.wait_please));

        RestClient.getAccountService().updateForgotPassword(phone, newPassword, smsCode, new Object(), new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                hideProgress();
                Toast.makeText(getActivity(), getString(R.string.password_changed), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                getActivity().startActivity(intent);
                getActivity().finish();
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

    private void activate(){
        showProgress(getString(R.string.wait_please));
        String smsCode = metSmscode.getText().toString();

        RestClient.getAccountService().activate(phone, password, smsCode, new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                hideProgress();
                GlobalSingleton globalSingleton = GlobalSingleton.getInstance(getActivity());
                globalSingleton.currentUser = user;
                globalSingleton.token = user.getToken();

                SessionHelper sessionHelper = new SessionHelper();
                sessionHelper.setPhone(user.getPhone());
                sessionHelper.setPassword(password);
                sessionHelper.setId(user.getId());
                sessionHelper.setToken(user.getToken());

                Intent intent = new Intent(getActivity(), CarDetailsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("NEW", true);
                getActivity().startActivity(intent);
                getActivity().finish();
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                if (error.getKind() == RetrofitError.Kind.HTTP) {
                    if(error.getResponse().getStatus() == 401){
                        Toast.makeText(getActivity(), getString(R.string.error_could_not_activate), Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getActivity(), getString(R.string.error_an_error_has_occurred_try_again), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(), getString(R.string.error_could_not_connect_to_server), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
