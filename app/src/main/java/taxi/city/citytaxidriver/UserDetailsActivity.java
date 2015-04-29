package taxi.city.citytaxidriver;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Service.ApiService;
import taxi.city.citytaxidriver.Utils.Helper;


public class UserDetailsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new UserDetailsFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class UserDetailsFragment extends Fragment implements View.OnClickListener {

        private EditText etLastName;
        private EditText etFirstName;
        private EditText etPhone;
        private EditText etPhoneExtra;
        private TextView tvTitle;
        private EditText etPassword;
        private EditText etEmail;
        private boolean isNew = false;

        Button btnSave;
        Button btnBack;
        Button btnExit;
        LinearLayout llBackExit;

        private User user;
        private UserUpdateTask mTask = null;


        public UserDetailsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_user_details, container, false);
            isNew = getActivity().getIntent().getBooleanExtra("NEW", false);

            user = User.getInstance();

            etLastName = (EditText)rootView.findViewById(R.id.editTextLastName);
            etFirstName = (EditText)rootView.findViewById(R.id.editTextFirstName);
            etEmail = (EditText)rootView.findViewById(R.id.editTextEmail);
            etPhone = (EditText) rootView.findViewById(R.id.textViewPhone);
            etPassword = (EditText) rootView.findViewById(R.id.editTextPassword);
            etPhoneExtra = (EditText) rootView.findViewById(R.id.textViewExtra);
            tvTitle = (TextView) rootView.findViewById(R.id.textViewTitle);
            llBackExit = (LinearLayout) rootView.findViewById(R.id.linearLayoutBackExitGroup);

            if (!isNew) {
                etLastName.setText(user.lastName);
                etFirstName.setText(user.firstName);
                etPassword.setText(user.password);
                etEmail.setText(user.email);
                String extra = user.phone.substring(0, 4);
                String phone = user.phone.substring(4);
                etPhone.setText(phone);
                etPhoneExtra.setText(extra);
            }

            btnSave = (Button)rootView.findViewById(R.id.buttonSave);
            btnBack = (Button)rootView.findViewById(R.id.buttonBack);
            btnExit = (Button)rootView.findViewById(R.id.buttonExit);

            btnSave.setOnClickListener(this);
            btnBack.setOnClickListener(this);
            btnExit.setOnClickListener(this);
            updateView();

            return rootView;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonSave:
                    updateTask();
                    break;
                default:
                    getActivity().finish();
                    break;
            }
        }

        private void updateView() {
            if (isNew) {
                llBackExit.setVisibility(View.GONE);
                btnSave.setText("Подтвердить");
                tvTitle.setText("Регистрация");
                etPhone.setEnabled(true);
                etPhoneExtra.setEnabled(true);
            } else {
                tvTitle.setText("Настройки Водителя");
                llBackExit.setVisibility(View.VISIBLE);
                btnSave.setText("Сохранить");
                etPhone.setEnabled(false);
                etPhoneExtra.setEnabled(false);
            }
        }

        private void updateTask() {
            if (mTask != null) return;

            String firstName = etFirstName.getText().toString();
            String lastName = etLastName.getText().toString();
            String phone = etPhoneExtra.getText().toString() + etPhone.getText().toString();
            String password = etPassword.getText().toString();
            String email = etEmail.getText().toString();

            if (firstName == null || firstName.length() < 2) {
                etFirstName.setError("Имя неправильно задано");
                etFirstName.requestFocus();
                return;
            }

            if (lastName == null || lastName.length() < 2) {
                etLastName.setError("Фамилия неправильно задано");
                etLastName.requestFocus();
                return;
            }

            if (email != null && !Helper.isValidEmailAddress(email)) {
                etEmail.setError("Email неправильно задано");
                etEmail.requestFocus();
                return;
            }

            if (phone.length() != 13) {
                etPhone.setError("Телефон должен состоять из 13 символов");
                etLastName.requestFocus();
                return;
            }

            if (password == null || password.length() < 3) {
                etPassword.setError("Пароль неправильно задан");
                etPassword.requestFocus();
                return;
            }

            JSONObject json = new JSONObject();
            try {
                json.put("first_name", firstName);
                json.put("last_name", lastName);
                json.put("email", email);
                json.put("password", password);
                if (isNew) {
                    json.put("phone", phone);
                    json.put("activation_code", "11111");
                }
            } catch (JSONException e)  {
                e.printStackTrace();
            }

            if (json.length() < 1) return;

            mTask = new UserUpdateTask(json);
            mTask.execute((Void) null);
        }

        private class UserUpdateTask extends AsyncTask<Void, Void, JSONObject> {

            private JSONObject mJson;

            UserUpdateTask(JSONObject json) {
                mJson = json;
            }

            @Override
            protected JSONObject doInBackground(Void... params) {
                // TODO: attempt authentication against a network service.
                // Simulate network access.
                if (isNew) return ApiService.getInstance().signUpRequest(mJson, "users/");
                return ApiService.getInstance().patchRequest(mJson, "users/" + user.id + "/");
            }

            @Override
            protected void onPostExecute(final JSONObject result) {
                mTask = null;
                int statusCode = -1;
                try {
                    if(Helper.isSuccess(result)) {
                        statusCode = result.getInt("status_code");
                    }
                    if (Helper.isSuccess(statusCode)) {
                        confirmUpdate(result);
                    } else if (statusCode == 400) {
                        if (result.has("phone")) {
                            etPhone.setError("Пользователь с таким номером уже существует");
                            etPhone.requestFocus();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Сервис недоступен", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onCancelled() {
                mTask = null;
            }
        }

        private void confirmUpdate(JSONObject object) {
            user.phone = etPhone.getText().toString();
            user.firstName = etPhone.getText().toString();
            user.lastName = etLastName.getText().toString();
            user.password = etPassword.getText().toString();
            user.email = etEmail.getText().toString();
            if (isNew) {
                try {
                    user.setUser(object);
                    if (object.has("token")) ApiService.getInstance().setToken(object.getString("token"));
                } catch (JSONException ignored) {}
                Intent intent = new Intent(getActivity(), ConfirmSignUpActivity.class);
                intent.putExtra("DATA", object.toString());
                startActivity(intent);
                getActivity().finish();
            }
        }
    }
}
