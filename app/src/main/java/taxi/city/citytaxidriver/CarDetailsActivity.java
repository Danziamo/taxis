package taxi.city.citytaxidriver;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import taxi.city.citytaxidriver.Core.CarEntity;
import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Service.ApiService;
import taxi.city.citytaxidriver.Utils.Helper;


public class CarDetailsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CarDetailsFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_car_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class CarDetailsFragment extends Fragment implements View.OnClickListener{
        private CarUpdateTask mUpdateTask = null;
        private FetchCarBrandTask mFetchTask = null;
        private boolean isNew = true;
        private User mUser;
        private int mBrandId = 0;
        private int mBrandModelId = 0;

        Spinner carBrandSpinner;
        Spinner carModelSpinner;
        Spinner carColorSpinner;
        EditText etCarColor;
        EditText etCarYear;
        EditText etTechPassport;
        EditText etCarNumber;
        EditText etPassportNumber;
        EditText etDriverLicense;
        TextView tvTitle;

        Button btnBack;
        Button btnSave;
        Button btnExit;

        LinearLayout llbackExitGroup;

        public CarDetailsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_car_details, container, false);

            isNew = getActivity().getIntent().getBooleanExtra("NEW", false);
            mUser = User.getInstance();
            carBrandSpinner = (Spinner) rootView.findViewById(R.id.spinnerCarBrand);
            carModelSpinner = (Spinner) rootView.findViewById(R.id.spinnerCarModel);
            etCarColor = (EditText) rootView.findViewById(R.id.spinnerCarColor);
            etCarYear = (EditText) rootView.findViewById(R.id.editTextCarYear);
            etTechPassport = (EditText) rootView.findViewById(R.id.editTextTechPassport);
            etDriverLicense = (EditText) rootView.findViewById(R.id.editTextDriverLicenseNumber);
            etCarNumber = (EditText) rootView.findViewById(R.id.editTextCarNumber);
            etPassportNumber = (EditText)rootView.findViewById(R.id.editTextPassportNumber);
            llbackExitGroup = (LinearLayout)rootView.findViewById(R.id.linearLayoutBackExitGroup);
            tvTitle = (TextView)rootView.findViewById(R.id.textViewCarSettingsTitle);

            btnSave = (Button)rootView.findViewById(R.id.buttonSave);
            btnBack = (Button)rootView.findViewById(R.id.buttonBack);
            btnExit = (Button)rootView.findViewById(R.id.buttonExit);

            btnBack.setOnClickListener(this);
            btnExit.setOnClickListener(this);
            btnSave.setOnClickListener(this);

            etDriverLicense.setText(mUser.driverLicenseNumber);
            etPassportNumber.setText(mUser.passportNumber);

            updateViews();
            updateTask(false);

            return rootView;
        }

        private void updateViews() {
            if (isNew) {
                llbackExitGroup.setVisibility(View.GONE);
                tvTitle.setText("Регистрация авто");
            } else {
                llbackExitGroup.setVisibility(View.VISIBLE);
                tvTitle.setText("Настройки Авто");
            }
        }

        private void updateTask(boolean update){
            if (mUpdateTask != null)
                return;

            JSONObject carJSON = new JSONObject();
            JSONObject userJSON = new JSONObject();
            if (update) {
                CarEntity carBrand = (CarEntity)carBrandSpinner.getSelectedItem();
                CarEntity carBrandModel = (CarEntity)carModelSpinner.getSelectedItem();

                if (carBrand == null || carBrand.id == 0)
                    return;
                if (carBrandModel == null || carBrandModel.id == 0)
                    return;
                try {
                    carJSON.put("driver", mUser.id);
                    carJSON.put("brand", carBrand.id + 1);
                    carJSON.put("brand_model", carBrandModel.id + 1);
                    carJSON.put("car_number", etCarNumber.getText().toString());
                    carJSON.put("year", etCarYear.getText().toString());
                    carJSON.put("color", etCarColor.getText().toString());
                    carJSON.put("technical_certificate", etTechPassport.getText().toString());
                    userJSON.put("passport_number", etPassportNumber.getText().toString());
                    userJSON.put("driver_license_number", etDriverLicense.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }

            mUpdateTask = new CarUpdateTask(update, carJSON, userJSON);
            mUpdateTask.execute((Void) null);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonSave:
                    updateTask(true);
                    break;
                default:
                    getActivity().finish();
                    break;
            }
        }

        private class CarUpdateTask extends AsyncTask<Void, Void, JSONObject> {
            private JSONObject carJson = new JSONObject();
            private boolean isUpdate = false;
            private JSONObject userJson = new JSONObject();

            CarUpdateTask(boolean update, JSONObject car, JSONObject user) {
                isUpdate = update;
                carJson = car;
                userJson = user;
            }

            @Override
            protected JSONObject doInBackground(Void... params) {
                if (isUpdate) {
                    JSONObject result = new JSONObject();
                    if (isNew) {
                        result = ApiService.getInstance().patchRequest(userJson, "users/" + mUser.id);
                        result = ApiService.getInstance().createCar(carJson, "usercars/");
                    } else {
                        result = ApiService.getInstance().patchRequest(userJson, "/users/" + mUser.id);
                        //result = ApiService.getInstance().patchRequest(carJson, "userscars/");
                    }
                    return result;
                } else {
                    return ApiService.getInstance().getDataFromGetRequest(null, "usercars/");
                }
            }

            @Override
            protected void onPostExecute(final JSONObject result) {
                mUpdateTask = null;
                int statusCode = -1;
                try {
                    if(Helper.isSuccess(result)) {
                        statusCode = result.getInt("status_code");
                    } else {
                        Toast.makeText(getActivity(), "Сервис недоступен", Toast.LENGTH_LONG).show();
                    }
                    if (!isUpdate) {
                        if (Helper.isSuccess(statusCode)) {
                            fillForms(result.getJSONArray("result"));
                        } else {
                            Toast.makeText(getActivity(), "Сервис недоступен", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if (Helper.isSuccess(statusCode)) {
                            finishUpdate();
                        } else {
                            Toast.makeText(getActivity(), "Сервис недоступен", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onCancelled() {
                mUpdateTask = null;
            }
        }

        private void finishUpdate() {
            if (isNew) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        }

        private void fillForms(JSONArray array) throws JSONException{
            if (array.length() < 1 && !isNew) return;
            if (isNew) {
                FillCarBrands();
            }
            for (int i = 0; i < array.length(); ++i) {
                JSONObject object = array.getJSONObject(i);
                if (object.getInt("driver") != mUser.id) continue;
                String color = Helper.getStringFromJson(object, "color");
                String techPassport = Helper.getStringFromJson(object, "technical_certificate");
                String carNumber = Helper.getStringFromJson(object, "car_number");
                String carYear = Helper.getStringFromJson(object, "year");
                etCarColor.setText(color);
                etTechPassport.setText(techPassport);
                etCarNumber.setText(carNumber);
                etCarYear.setText(carYear);
                mBrandId = object.has("brand") ? object.getInt("brand") : 0;
                mBrandModelId = object.has("brand_model") ? object.getInt("brand_model") : 0;
                FillCarBrands();
                break;
            }
        }

        private class FetchCarBrandTask extends AsyncTask<Void, Void, JSONArray> {
            private JSONObject json = new JSONObject();
            String api;
            CarEntity carBrand;
            boolean isModel;

            FetchCarBrandTask(boolean isModel) {
                carBrand = (CarEntity)carBrandSpinner.getSelectedItem();
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
                mFetchTask = null;
                if (result != null) {
                    if (isModel) {
                        FillBrandCarModelSpinnerArray(result);
                    } else {
                        FillBrandSpinnerArray(result);
                    }
                } else {
                    Toast.makeText(getActivity(), "Не удалось оторбразить список машин", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected void onCancelled() {
                mFetchTask = null;
            }
        }

        private void FillCarBrands() {
            if (mFetchTask != null) {
                return;
            }

            mFetchTask = new FetchCarBrandTask(false);
            mFetchTask.execute((Void) null);
        }

        private void FillCarBrandsModels() {
            if (mFetchTask != null) {
                return;
            }

            mFetchTask = new FetchCarBrandTask(true);
            mFetchTask.execute((Void) null);
        }

        private void FillBrandCarModelSpinnerArray(JSONArray array) {
            List<CarEntity> list = new ArrayList<>();

            try {
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject row = array.getJSONObject(i);
                    list.add(new CarEntity(row.getInt("id"), row.getString("brand_model_name")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ArrayAdapter<CarEntity> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            carModelSpinner.setAdapter(adapter);
            if (mBrandModelId <= array.length()) {
                carModelSpinner.setSelection(mBrandModelId - 1);
                carModelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        }

        private void FillBrandSpinnerArray(JSONArray array) {
            List<CarEntity> list = new ArrayList<>();
            try {
                for (int i = 0; i < array.length(); ++i) {
                    JSONObject row = array.getJSONObject(i);
                    list.add(new CarEntity(row.getInt("id"), row.getString("brand_name")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ArrayAdapter<CarEntity> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            carBrandSpinner.setAdapter(adapter);
            carBrandSpinner.setSelection(mBrandId - 1);
            carBrandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    FillCarBrandsModels();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }


    }
}
