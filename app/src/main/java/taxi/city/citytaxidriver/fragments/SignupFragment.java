package taxi.city.citytaxidriver.fragments;

import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.models.GlobalSingleton;
import taxi.city.citytaxidriver.models.User;
import taxi.city.citytaxidriver.networking.RestClient;
import taxi.city.citytaxidriver.networking.model.NUser;
import taxi.city.citytaxidriver.utils.Constants;

public class SignupFragment extends BaseFragment implements View.OnClickListener{

    private MaterialEditText metPhoneNumber;
    private MaterialEditText metPassword;
    private AppCompatSpinner spPhonePrefix;

    public SignupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        view.findViewById(R.id.tvForgotPassword).setVisibility(View.GONE);

        metPhoneNumber = (MaterialEditText) view.findViewById(R.id.metPhoneNumber);
        metPassword = (MaterialEditText) view.findViewById(R.id.metPassword);

        spPhonePrefix = (AppCompatSpinner) view.findViewById(R.id.spCodNumber);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, Constants.PHONE_PREFIXES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPhonePrefix = (AppCompatSpinner) view.findViewById(R.id.spCodNumber);
        spPhonePrefix.setAdapter(adapter);

        Button btnSubmit = (Button) view.findViewById(R.id.btnSubmit);
        btnSubmit.setText(getString(R.string.signup_submit));
        btnSubmit.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSubmit:
                signup();
                break;
        }
    }


    public void signup(){
        final String phone = spPhonePrefix.getSelectedItem().toString() + metPhoneNumber.getText().toString();
        final String password = metPassword.getText().toString();

        if (phone.length() != 13) {
            metPhoneNumber.setError(getString(R.string.error_invalid_phone_format));
            metPhoneNumber.requestFocus();
            return;
        }

        if (password.length() < 4) {
            metPassword.setError(getString(R.string.error_invalid_password));
            metPassword.requestFocus();
            return;
        }

        showProgress(getString(R.string.wait_please));

        NUser nuser = new NUser();
        nuser.phone = phone;
        nuser.password = password;
        nuser.firstName = getString(R.string.signup_default_first_name);
        nuser.lastName = getString(R.string.signup_default_last_name);

        RestClient.getUserService().add(nuser, new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                hideProgress();
                GlobalSingleton globalSingleton = GlobalSingleton.getInstance(getActivity());
                globalSingleton.currentUser = user;

                String backStateName = getActivity().getFragmentManager().getClass().getName();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, AuthorizationFragment.newInstance(phone, password, false))
                        .addToBackStack(backStateName)
                        .commit();
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                String message = "";
                if (error.getKind() == RetrofitError.Kind.HTTP) {
                    String result = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                    try {
                        JSONObject json = new JSONObject(result);
                        if(json.has("phone")){
                            metPhoneNumber.setError(getString(R.string.error_phone_must_be_unique));
                            return ;
                        }
                    } catch (JSONException e) {
                        message = getString(R.string.error_an_error_has_occurred_try_again);
                        Crashlytics.logException(e);
                    }
                } else {
                    message = getString(R.string.error_could_not_connect_to_server);
                }
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
