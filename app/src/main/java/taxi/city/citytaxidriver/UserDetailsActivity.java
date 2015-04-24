package taxi.city.citytaxidriver;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import taxi.city.citytaxidriver.Core.Order;
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
        private EditText etPassword;

        Button btnSave;
        Button btnBack;
        Button btnExit;

        private User user;
        private UserUpdateTask mTask = null;


        public UserDetailsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_user_details, container, false);

            user = User.getInstance();

            etLastName = (EditText)rootView.findViewById(R.id.editTextLastName);
            etFirstName = (EditText)rootView.findViewById(R.id.editTextFirstName);
            etPhone = (EditText) rootView.findViewById(R.id.editTextPhone);
            etPassword = (EditText) rootView.findViewById(R.id.editTextPassword);

            etLastName.setText(user.lastName);
            etFirstName.setText(user.firstName);
            etPassword.setText(user.password);
            etPhone.setText(user.phone);

            btnSave = (Button)rootView.findViewById(R.id.buttonSave);
            btnBack = (Button)rootView.findViewById(R.id.buttonBack);
            btnExit = (Button)rootView.findViewById(R.id.buttonExit);

            btnSave.setOnClickListener(this);
            btnBack.setOnClickListener(this);
            btnExit.setOnClickListener(this);

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

        private void updateTask() {
            if (mTask != null) return;

            JSONObject json = new JSONObject();
            try {
                json.put("first_name", etFirstName.getText().toString());
                json.put("last_name", etLastName.getText().toString());
                //json.put("phone", etPhone.getText().toString());
                //json.put("password", etPassword.getText().toString());
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
                return ApiService.getInstance().patchRequest(mJson, "users/" + user.id + "/");
            }

            @Override
            protected void onPostExecute(final JSONObject result) {
                mTask = null;
                int statusCode = -1;
                try {
                    if(result != null && result.has("status_code")) {
                        statusCode = result.getInt("status_code");
                    }

                    if (Helper.isSuccess(statusCode)) {
                        confirmUpdate();
                    } else if (statusCode == 400) {
                        if (result.has("phone")) {
                            Toast.makeText(getActivity(), "Пользователь с таким номером уже существует", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Сервис недоступен", Toast.LENGTH_LONG).show();
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

        private void confirmUpdate() {
            user.phone = etPhone.getText().toString();
            user.firstName = etPhone.getText().toString();
            user.lastName = etLastName.getText().toString();
            user.password = etPassword.getText().toString();
        }
    }
}
