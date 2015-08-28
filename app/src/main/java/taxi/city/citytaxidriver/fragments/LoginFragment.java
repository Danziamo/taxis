package taxi.city.citytaxidriver.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.rengwuxian.materialedittext.MaterialEditText;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.MainActivity;
import taxi.city.citytaxidriver.models.GlobalSingleton;
import taxi.city.citytaxidriver.models.OnlineStatus;
import taxi.city.citytaxidriver.models.Role;
import taxi.city.citytaxidriver.models.Session;
import taxi.city.citytaxidriver.models.User;
import taxi.city.citytaxidriver.networking.RestClient;
import taxi.city.citytaxidriver.networking.model.UserStatus;
import taxi.city.citytaxidriver.utils.Constants;
import taxi.city.citytaxidriver.utils.SessionHelper;

public class LoginFragment extends BaseFragment {

    AppCompatSpinner spinner;
    private MaterialEditText phoneView;
    private MaterialEditText passwordView;
    private TextView forgotView;


    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        setActionBarTitle("Войти");

        phoneView = (MaterialEditText) view.findViewById(R.id.metPhoneNumber);
        passwordView = (MaterialEditText) view.findViewById(R.id.metPassword);
        forgotView = (TextView)view.findViewById(R.id.tvForgotPassword);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, Constants.PHONE_PREFIXES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = (AppCompatSpinner) view.findViewById(R.id.spCodNumber);
        spinner.setAdapter(adapter);

        Button btnSignIn = (Button)view.findViewById(R.id.btnSubmit);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });


        forgotView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String backStateName = getActivity().getFragmentManager().getClass().getName();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, ForgotPasswordFragment.newInstance(phoneView.getText().toString(), spinner.getSelectedItemPosition()))
                        .addToBackStack(backStateName)
                        .commit();
            }
        });
        return view;
    }

    private void attemptLogin() {
        String phone = spinner.getSelectedItem().toString() + phoneView.getText().toString();
        String password = passwordView.getText().toString();
        boolean isError = false;

        if (phoneView.getText().toString().isEmpty()) {
            if (spinner.getSelectedItem().toString().equals("+996")) {
                phoneView.setError("(XXX) XXX-XXX");
            } else {
                phoneView.setError("Не должен быть пустым");
            }
            isError = true;
        }

        if (password.length() < 4) {
            passwordView.setError("Пароль не менее 4 символов");
            isError = true;
        }

        if (!isError) {
            Session session = new Session();
            session.setPhone(phone);
            session.setPassword(password);
            RestClient.getSessionService().login(session, new Callback<User>() {
                @Override
                public void success(User user, Response response) {
                    GlobalSingleton.getInstance(getActivity()).token = user.getToken();
                    GlobalSingleton.getInstance(getActivity()).currentUser = user;
                    saveSessionPreferencesNew(user);

                    if (user.hasActiveOrder() && user.getActiveOrder() != null) {
                        GlobalSingleton.getInstance(getActivity()).currentOrderModel = user.getActiveOrder();
                    }

                    UserStatus userStatus = new UserStatus();
                    userStatus.iosToken = null;
                    userStatus.onlineStatus = OnlineStatus.ONLINE;
                    userStatus.role = Role.USER;
                    RestClient.getUserService().updateStatus(user.getId(), userStatus, new Callback<User>() {
                        @Override
                        public void success(User user, Response response) {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Crashlytics.logException(error);
                            Toast.makeText(getActivity(), error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    });
                }

                @Override
                public void failure(RetrofitError error) {
                    String message = "";
                    if (error.getKind() == RetrofitError.Kind.HTTP) {
                        String json = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                        if (json.toLowerCase().contains("username or password")) {
                            message = "Телефон или пароль неверны";
                        } else if (json.toLowerCase().contains("account")) {
                            message = "Аккаунт не активирован";
                            String backStateName = getActivity().getFragmentManager().getClass().getName();
                            getActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.container, AuthorizationFragment.newInstance(spinner.getSelectedItem().toString() + phoneView.getText().toString()
                                            , passwordView.getText().toString(), false))
                                    .addToBackStack(backStateName)
                                    .commit();
                        }
                    } else {
                        message = "Не удалось подключится к серверу";
                    }
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveSessionPreferencesNew(User user) {
        SessionHelper sessionHelper = new SessionHelper();
        sessionHelper.setPhone(user.getPhone());
        sessionHelper.setPassword(passwordView.getText().toString());
        sessionHelper.setId(user.getId());
        sessionHelper.setToken(user.getToken());
    }

}
