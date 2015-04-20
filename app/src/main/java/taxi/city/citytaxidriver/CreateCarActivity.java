package taxi.city.citytaxidriver;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Service.ApiService;


public class CreateCarActivity extends ActionBarActivity {

    Spinner spinnerCarBrand;
    Spinner spinnerCarBrandModel;
    EditText editTextCarColor;
    EditText editTextCarNumber;
    EditText editTextCarSeriesNumber;
    EditText editTextCarTechPassport;
    EditText editTextIdInfo;
    EditText editTextCarYear;
    UserCreateCarTask task = null;
    FetchCarBrandTask fetchTask = null;

    private View mProgressView;
    private View mLoginFormView;

    class Entity {
        public int id;
        public String name;

        public Entity(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_car);
        Initialize();
        FillCarBrands();
    }

    private void Initialize() {
        spinnerCarBrand = (Spinner)findViewById(R.id.spinnerCarBrand);
        spinnerCarBrandModel = (Spinner)findViewById(R.id.spinnerCarBrandModel);
        editTextCarColor = (EditText)findViewById(R.id.etCarColor);
        editTextCarNumber = (EditText)findViewById(R.id.etCarNumber);
        editTextCarSeriesNumber = (EditText)findViewById(R.id.etNumberOfLicense);
        editTextCarTechPassport = (EditText)findViewById(R.id.etTechPassport);
        editTextIdInfo = (EditText) findViewById(R.id.etPassportId);
        editTextCarYear = (EditText) findViewById(R.id.etCarYear);

        mProgressView = findViewById(R.id.progressBarCreateCar);

        Button btn = (Button) findViewById(R.id.btnSaveCar);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSaveCar();
            }
        });
    }

    private void FillCarBrands() {
        if (fetchTask != null) {
            return;
        }

        fetchTask = new FetchCarBrandTask(false);
        fetchTask.execute((Void) null);
    }

    private void FillCarBrandsModels() {
        if (fetchTask != null) {
            return;
        }

        fetchTask = new FetchCarBrandTask(true);
        fetchTask.execute((Void) null);
    }

    private void FillBrandSpinnerArray(JSONArray array) {
        List<Entity> list = new ArrayList<>();
        try {
            for (int i = 0; i < array.length(); ++i) {
                JSONObject row = array.getJSONObject(i);
                list.add(new Entity(row.getInt("id"), row.getString("brand_name")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<Entity> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCarBrand.setAdapter(adapter);
        spinnerCarBrand.setSelection(0);
        spinnerCarBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FillCarBrandsModels();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void FillBrandCarModelSpinnerArray(JSONArray array) {
        List<Entity> list = new ArrayList<>();

        try {
            for (int i = 0; i < array.length(); ++i) {
                JSONObject row = array.getJSONObject(i);
                list.add(new Entity(row.getInt("id"), row.getString("brand_model_name")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayAdapter<Entity> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCarBrandModel.setAdapter(adapter);
        spinnerCarBrandModel.setSelection(0);
        spinnerCarBrandModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void attemptSaveCar() {
        if (task != null) {
            return;
        }

        Entity carBrand = (Entity)spinnerCarBrand.getSelectedItem();
        Entity carBrandModel = (Entity)spinnerCarBrandModel.getSelectedItem();

        if (carBrand == null || carBrand.id == 0)
            return;
        if (carBrandModel == null || carBrandModel.id == 0)
            return;

        int carBrandId = carBrand.id;
        int carBrandModelId = carBrandModel.id;
        String carColor = editTextCarColor.getText().toString();
        String carNumber = editTextCarNumber.getText().toString();
        String carYear = editTextCarYear.getText().toString();
        String carSeries = editTextCarSeriesNumber.getText().toString();
        String carTechPassport = editTextCarTechPassport.getText().toString();
        String passportId = editTextIdInfo.getText().toString();

        task = new UserCreateCarTask(carBrandId, carBrandModelId, carColor, carNumber, carYear, carSeries, carTechPassport, passportId);
        task.execute((Void) null);
    }

    private class FetchCarBrandTask extends AsyncTask<Void, Void, JSONArray> {
        private JSONObject json = new JSONObject();
        String api;
        Entity carBrand;
        boolean isModel;

        FetchCarBrandTask(boolean isModel) {
            carBrand = (Entity)spinnerCarBrand.getSelectedItem();
            this.isModel = isModel;
            if (isModel) {
                api = "cars/carbrandmodels/?car_brand=" + carBrand.id;
            } else {
                api = "cars/carbrands/";
            }
        }

        @Override
        protected JSONArray doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.

            return ApiService.getInstance().fetchCarBrand(api);
        }

        @Override
        protected void onPostExecute(final JSONArray result) {
            fetchTask = null;
            if (result != null) {
                if (isModel) {
                    FillBrandCarModelSpinnerArray(result);
                } else {
                    FillBrandSpinnerArray(result);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Не удалось оторбразить список машин", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            task = null;
            showProgress(false);
        }
    }

    private class UserCreateCarTask extends AsyncTask<Void, Void, JSONObject> {
        private JSONObject json = new JSONObject();

        UserCreateCarTask(int brand, int model, String color, String number, String year, String serial, String passport, String id) {
            try {
                json.put("driver", User.getInstance().id);
                json.put("brand", brand);
                json.put("brand_model", model);
                json.put("color", color);
                json.put("year", year);
                json.put("car_number", number);
                json.put("technical_certificate", "aaaaaaa");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.

            return ApiService.getInstance().createCar(json, "usercars/");
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            task = null;
            showProgress(false);
            int statusCode = -1;
            try {
                if(result != null && result.has("status_code")) {
                    statusCode = result.getInt("status_code");
                }

                if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_ACCEPTED || statusCode == HttpStatus.SC_CREATED) {
                    ConfirmCreation();
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

    private void ConfirmCreation() {
        Intent intent = new Intent(CreateCarActivity.this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
