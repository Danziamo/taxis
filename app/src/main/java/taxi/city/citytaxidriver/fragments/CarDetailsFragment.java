package taxi.city.citytaxidriver.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import taxi.city.citytaxidriver.MapsActivity;
import taxi.city.citytaxidriver.core.CarEntity;
import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.service.ApiService;
import taxi.city.citytaxidriver.utils.Helper;

public class CarDetailsFragment extends Fragment implements View.OnClickListener{

    private static CarDetailsFragment mInstance = null;
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
    EditText etDriverLicenseSeries;
    EditText etPassportSeries;
    TextView tvTitle;

    SweetAlertDialog pDialog;

    Button btnBack;
    Button btnSave;

    int userCarId = 0;

    public static CarDetailsFragment newInstance() {
        return new CarDetailsFragment();
    }

    public static CarDetailsFragment getInstance() {
        if (mInstance == null) {
            mInstance = new CarDetailsFragment();
        }
        return mInstance;
    }

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
        etDriverLicenseSeries = (EditText)rootView.findViewById(R.id.editTextDriverLicenseSeries);
        etCarNumber = (EditText) rootView.findViewById(R.id.editTextCarNumber);
        etPassportNumber = (EditText)rootView.findViewById(R.id.editTextPassportNumber);
        etPassportSeries = (EditText)rootView.findViewById(R.id.editTextPassportSeries);
        //tvTitle = (TextView)rootView.findViewById(R.id.textViewCarSettingsTitle);

        btnSave = (Button)rootView.findViewById(R.id.buttonSave);
        btnBack = (Button)rootView.findViewById(R.id.buttonBack);

        btnBack.setOnClickListener(this);
        btnBack.setVisibility(isNew ? View.VISIBLE : View.GONE);
        btnSave.setOnClickListener(this);

        String driverLicenseExtra = mUser.driverLicenseNumber != null && mUser.driverLicenseNumber.length() > 2
                ? mUser.driverLicenseNumber.substring(0, 2) : null;
        String driverLicense = mUser.driverLicenseNumber != null && mUser.driverLicenseNumber.length() > 2
                ? mUser.driverLicenseNumber.substring(2) : null;
        String passportExtra = mUser.passportNumber != null && mUser.passportNumber.length() > 2
                ? mUser.passportNumber.substring(0, 2) : null;
        String passport = mUser.passportNumber != null && mUser.passportNumber.length() > 2
                ? mUser.passportNumber.substring(2) : null;

        etDriverLicenseSeries.setText(driverLicenseExtra);
        etDriverLicense.setText(driverLicense);
        etPassportSeries.setText(passportExtra);
        etPassportNumber.setText(passport);
        if (!isNew && mUser.car != null) {
            etCarColor.setText(mUser.car.color);
            etCarNumber.setText(mUser.car.number);
            etCarYear.setText(mUser.car.year);
            etTechPassport.setText(mUser.car.technicalCertificate);
            mBrandId = mUser.car.brandId;
            mBrandModelId = mUser.car.modelId;
            userCarId = mUser.car.id;
        }
        fillCarBrands();

        return rootView;
    }

    private void updateTask(){
        if (mUpdateTask != null)
            return;

        JSONObject carJSON = new JSONObject();
        JSONObject userJSON = new JSONObject();

        CarEntity carBrand = (CarEntity)carBrandSpinner.getSelectedItem();
        CarEntity carBrandModel = (CarEntity)carModelSpinner.getSelectedItem();

        if (carBrand == null || carBrand.id == 0) {
            carBrandSpinner.requestFocus();
            Toast.makeText(getActivity(), "Выберите машину", Toast.LENGTH_LONG).show();
            return;
        }

        String passportNumberExtra = etPassportSeries.getText().toString();
        String passportNumber = passportNumberExtra + etPassportNumber.getText().toString();
        String driverLicenseExtra = etDriverLicenseSeries.getText().toString();
        String driverLicense = driverLicenseExtra + etDriverLicense.getText().toString();
        String techPassport = etTechPassport.getText().toString();
        String carNumber =etCarNumber.getText().toString();
        String color = etCarColor.getText().toString();
        String year = etCarYear.getText().toString();

        if (passportNumberExtra.length() != 2) {
            etPassportSeries.setError("Неверно задано. 2 символа");
            etPassportSeries.requestFocus();
            return;
        }

        if (passportNumber.length() < 6) {
            etPassportNumber.setError("Неверно задано");
            etPassportNumber.requestFocus();
            return;
        }

        if (driverLicenseExtra.length() != 2) {
            etDriverLicenseSeries.setError("Неверно задано. 2 символа");
            etDriverLicenseSeries.requestFocus();
            return;
        }

        if (driverLicense.length() < 6) {
            etDriverLicense.setError("Неверно задано");
            etDriverLicense.requestFocus();
            return;
        }

        if (techPassport.length() < 6) {
            etTechPassport.setError("Неверно задано");
            etTechPassport.requestFocus();
            return;
        }

        if (carNumber.length() < 6) {
            etCarNumber.setError("Неверно задано");
            etCarNumber.requestFocus();
            return;
        }

        if (!Helper.isYearValid(year)) {
            etCarYear.setError("Неверно задано");
            etCarYear.requestFocus();
            return;
        }

        if (color.length() < 3) {
            etCarColor.setError("Неверно задано");
            etCarColor.requestFocus();
            return;
        }

        try {
            JSONObject brandJson = new JSONObject();
            brandJson.put("id", carBrand.id);
            brandJson.put("brand_name", carBrand.name);

            JSONObject modelJson = new JSONObject();
            modelJson.put("id", carBrandModel.id);
            modelJson.put("brand_model_name", carBrandModel.name);
            //modelJson.put("car_brand", brandJson);

            carJSON.put("driver", mUser.id);
            /*carJSON.put("brand", carBrand.id);
            carJSON.put("brand_model", carBrandModel.id);*/
            carJSON.put("brand", brandJson);
            carJSON.put("brand_model", modelJson);
            carJSON.put("car_number", carNumber);
            carJSON.put("year", year);
            carJSON.put("color", color);
            carJSON.put("technical_certificate", techPassport);
            userJSON.put("passport_number", passportNumber);
            userJSON.put("driver_license_number", driverLicense);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        showProgress(true);
        mUpdateTask = new CarUpdateTask(carJSON, userJSON);
        mUpdateTask.execute((Void) null);
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

    private class CarUpdateTask extends AsyncTask<Void, Void, JSONObject> {
        private JSONObject carJson = new JSONObject();
        private JSONObject userJson = new JSONObject();

        CarUpdateTask(JSONObject car, JSONObject user) {
            carJson = car;
            userJson = user;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject result = new JSONObject();
            if (isNew) {
                result = ApiService.getInstance().patchRequest(userJson, "users/" + mUser.id + "/");
                result = ApiService.getInstance().createCar(carJson, "usercars/");
                return result;
            } else {
                result = ApiService.getInstance().patchRequest(userJson, "users/" + mUser.id + "/");
                result = ApiService.getInstance().patchRequest(carJson, "usercars/" + userCarId + "/");
                return result;
            }
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            showProgress(false);
            mUpdateTask = null;
            int statusCode = -1;
            try {
                if(Helper.isSuccess(result)) {
                    statusCode = result.getInt("status_code");
                } else {
                    Toast.makeText(getActivity(), "Сервис недоступен", Toast.LENGTH_LONG).show();
                }
                if (Helper.isSuccess(statusCode)) {
                    finishUpdate();
                } else {
                    Toast.makeText(getActivity(), "Сервис недоступен", Toast.LENGTH_LONG).show();
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
        mUser.driverLicenseNumber = etDriverLicenseSeries.getText().toString() + etDriverLicense.getText().toString();
        mUser.passportNumber = etPassportSeries.getText().toString() + etPassportNumber.getText().toString();
        if (isNew) {
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private class FetchCarBrandTask extends AsyncTask<Void, Void, JSONArray> {
        String api;
        CarEntity carBrand;
        boolean isModel;

        FetchCarBrandTask(boolean isModel) {
            this.isModel = isModel;
            if (isModel) {
                carBrand = (CarEntity)carBrandSpinner.getSelectedItem();
                api = "cars/carbrandmodels/?limit=200&car_brand=" + carBrand.id;
            } else {
                api = "cars/carbrands/?limit=200&ordering=brand_name";
            }
        }

        @Override
        protected JSONArray doInBackground(Void... params) {
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

    private void fillCarBrands() {
        if (mFetchTask != null || User.getInstance() == null || User.getInstance().id == 0) {
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

        CarEntity tempEntity = new CarEntity(mBrandModelId, "");
        int position = adapter.getPosition(tempEntity);

        carModelSpinner.setAdapter(adapter);
        carModelSpinner.setSelection(position);
        carModelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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

        CarEntity tempEntity = new CarEntity(mBrandId, "");
        int position = adapter.getPosition(tempEntity);

        carBrandSpinner.setAdapter(adapter);
        carBrandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FillCarBrandsModels();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        carBrandSpinner.setSelection(position);
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
            if (pDialog != null) pDialog.dismissWithAnimation();
        }
    }
}
