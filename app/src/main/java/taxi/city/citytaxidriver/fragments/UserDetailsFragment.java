package taxi.city.citytaxidriver.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import taxi.city.citytaxidriver.CarDetailsActivity;
import taxi.city.citytaxidriver.ConfirmSignUpActivity;
import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.service.ApiService;
import taxi.city.citytaxidriver.utils.Helper;

public class UserDetailsFragment extends Fragment implements View.OnClickListener {

    private EditText etLastName;
    private EditText etFirstName;
    private EditText etPhone;
    private EditText etPhoneExtra;
    private EditText etDoB;
    private TextView tvTitle;
    private EditText etPassword;
    private EditText etEmail;
    private boolean isNew = false;

    private SweetAlertDialog pDialog;
    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;

    Button btnSave;
    Button btnBack;

    private User user;
    private UserUpdateTask mTask = null;


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

        user = User.getInstance();

        etLastName = (EditText)rootView.findViewById(R.id.editTextLastName);
        etFirstName = (EditText)rootView.findViewById(R.id.editTextFirstName);
        etEmail = (EditText)rootView.findViewById(R.id.editTextEmail);
        etPhone = (EditText) rootView.findViewById(R.id.textViewPhone);
        etPassword = (EditText) rootView.findViewById(R.id.editTextPassword);
        etPhoneExtra = (EditText) rootView.findViewById(R.id.textViewExtra);
        //tvTitle = (TextView) rootView.findViewById(R.id.textViewTitle);
        etDoB = (EditText) rootView.findViewById(R.id.editTextDoB);
        etDoB.setInputType(InputType.TYPE_NULL);
        etDoB.setOnClickListener(this);

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
            etLastName.setText(user.lastName);
            etFirstName.setText(user.firstName);
            etPassword.setText(user.password);
            etEmail.setText(user.email);
            etDoB.setText(user.dob == null || user.dob.equals("null") ? null : user.dob);
            String extra = user.phone.substring(0, 4);
            String phone = user.phone.substring(4);
            etPhone.setText(phone);
            etPhoneExtra.setText(extra);
            etPhone.setEnabled(false);
            etPhoneExtra.setEnabled(false);
        }

        btnSave = (Button)rootView.findViewById(R.id.buttonSave);
        btnBack = (Button)rootView.findViewById(R.id.buttonBack);
        btnBack.setVisibility(isNew ? View.VISIBLE : View.GONE);

        btnSave.setOnClickListener(this);
        btnBack.setOnClickListener(this);
//        updateView();

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        setDateTimePicker();
        return rootView;
    }

    private void setDateTimePicker() {
        etDoB.setOnClickListener(this);

        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Dialog_NoActionBar ,new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                etDoB.setText(dateFormatter.format(newDate.getTime()));
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSave:
                updateTask();
                break;
            case R.id.editTextDoB:
                datePickerDialog.show();
                break;
            default:
                getActivity().finish();
                break;
        }
    }

    private void updateTask() {
        if (mTask != null) return;

        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();
        String phone = etPhoneExtra.getText().toString() + etPhone.getText().toString();
        String password = etPassword.getText().toString();
        String email = etEmail.getText().toString();
        String dob = etDoB.getText().toString();

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

        if (email != null && email.length() > 0 && !Helper.isValidEmailAddress(email)) {
            etEmail.setError("Email неправильно задан");
            etEmail.requestFocus();
            return;
        }

        if (isNew && phone.length() != 13) {
            etPhone.setError("Телефон должен состоять из 13 символов");
            etPhone.requestFocus();
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
            json.put("email", email.length() == 0 ? JSONObject.NULL : email);
            json.put("date_of_birth", dob);
            json.put("password", password);
            if (isNew) json.put("phone", phone);
        } catch (JSONException e)  {
            e.printStackTrace();
        }

        if (json.length() < 1) return;

        if (!Helper.isNetworkAvailable(getActivity())) {
            Toast.makeText(getActivity(), "Нету подключения к интернету", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);
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
            if (isNew) return ApiService.getInstance().signUpRequest(mJson, "users/");
            return ApiService.getInstance().patchRequest(mJson, "users/" + user.id + "/");
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            mTask = null;
            showProgress(false);
            int statusCode = -1;
            try {
                if(result != null && result.has("status_code")) {
                    statusCode = result.getInt("status_code");
                }
                if (Helper.isSuccess(statusCode)) {
                    confirmUpdate(result);
                } else if (statusCode == HttpStatus.SC_BAD_REQUEST) {
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
        user.phone = etPhoneExtra.getText().toString() + etPhone.getText().toString();
        user.firstName = etFirstName.getText().toString();
        user.lastName = etLastName.getText().toString();
        user.password = etPassword.getText().toString();
        user.email = etEmail.getText().toString();
        user.dob = etDoB.getText().toString();
        if (isNew) {
            try {
                user.setUser(object);
                if (object.has("token")) ApiService.getInstance().setToken(object.getString("token"));
            } catch (JSONException ignored) {}
            Intent intent = new Intent(getActivity(), ConfirmSignUpActivity.class);
            intent.putExtra("SIGNUP", true);
            startActivity(intent);
            getActivity().finish();
        }
    }

    public void showProgress(final boolean show) {
        if (show) {
            pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper()
                    .setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Сохранение");
            pDialog.setCancelable(true);
            pDialog.show();
        } else {
            pDialog.dismissWithAnimation();
        }
    }
}