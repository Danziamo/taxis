package taxi.city.citytaxidriver.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import taxi.city.citytaxidriver.App;
import taxi.city.citytaxidriver.MainActivity;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.db.models.Brand;
import taxi.city.citytaxidriver.db.models.BrandModel;
import taxi.city.citytaxidriver.models.Car;
import taxi.city.citytaxidriver.models.GlobalSingleton;
import taxi.city.citytaxidriver.models.User;
import taxi.city.citytaxidriver.networking.RestClient;
import taxi.city.citytaxidriver.networking.model.NCar;
import taxi.city.citytaxidriver.utils.Helper;

public class CarDetailsFragment extends BaseFragment implements View.OnClickListener{

    private static CarDetailsFragment mInstance = null;
    private boolean isNew = true;
    private User mUser;
    private Car mCar;

    Spinner carBrandSpinner;
    Spinner carModelSpinner;

    ArrayAdapter<Brand> brandArrayAdapter;
    ArrayAdapter<BrandModel> brandModelArrayAdapter;

    EditText etCarColor;
    EditText etCarYear;
    EditText etCarNumber;
    EditText etDriverLicense;

    Button btnSave;

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

        mUser = GlobalSingleton.getInstance().currentUser;
        mCar = mUser.getCar();

        carBrandSpinner = (Spinner) rootView.findViewById(R.id.spinnerCarBrand);
        carModelSpinner = (Spinner) rootView.findViewById(R.id.spinnerCarModel);
        etCarYear = (EditText) rootView.findViewById(R.id.etCarYear);
        etCarColor = (EditText) rootView.findViewById(R.id.spinnerCarColor);
        etDriverLicense = (EditText) rootView.findViewById(R.id.editTextDriverLicenseNumber);
        etCarNumber = (EditText) rootView.findViewById(R.id.editTextCarNumber);

        btnSave = (Button)rootView.findViewById(R.id.buttonSave);
        btnSave.setOnClickListener(this);

        String driverLicense = mUser.getDriverLicenseNumber();
        etDriverLicense.setText(driverLicense);

        if (!isNew && mCar != null) {
            etCarColor.setText(mCar.getColor());
            etCarNumber.setText(mCar.getNumber());
            etCarYear.setText(String.valueOf(mCar.getYear()));
        }

        rootView.findViewById(R.id.carContainer).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });

        brandArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<Brand>());
        brandArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        carBrandSpinner.setAdapter(brandArrayAdapter);


        brandModelArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<BrandModel>());
        brandModelArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        carModelSpinner.setAdapter(brandModelArrayAdapter);

        carBrandSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (getActivity().getCurrentFocus() != null)
                    inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                return false;
            }
        });

        carModelSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager inputMethodManager = (InputMethodManager)  getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (getActivity().getCurrentFocus() != null)
                    inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchBrands(true);
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void createCarCreateAnalyticsEvent(String msg){
        if (isNew) {
            App.getDefaultTracker().send(new HitBuilders.EventBuilder()
                    .setCategory("car_create")
                    .setAction("car_create_error")
                    .setLabel("Car Create Error: " + msg)
                    .build());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSave:
                updateCar();
                break;
            default:
                getActivity().finish();
                break;
        }
    }

    private void updateCar(){
        if (isNew){
            App.getDefaultTracker().send(new HitBuilders.EventBuilder()
                    .setCategory("car_create")
                    .setAction("car_create")
                    .setLabel("Car save button pressed")
                    .build());
        }

        Brand brand = (Brand) carBrandSpinner.getSelectedItem();
        BrandModel brandModel = (BrandModel) carModelSpinner.getSelectedItem();

        if (brand == null || brand.getBrandId() == 0) {
            carBrandSpinner.requestFocus();
            Toast.makeText(getActivity(), "Выберите марку", Toast.LENGTH_LONG).show();
            createCarCreateAnalyticsEvent("Выберите марку");
            return;
        }

        if(brandModel == null || brandModel.getBrandModelId() == 0){
            carBrandSpinner.requestFocus();
            Toast.makeText(getActivity(), "Выберите модель", Toast.LENGTH_LONG).show();
            createCarCreateAnalyticsEvent("Выберите модель");
            return;
        }

        NCar newCar;
        if(isNew){
            newCar = new NCar();
            newCar.driver = mUser.getId();
        } else{
            newCar = new NCar(mCar);
        }
        newCar.brand = brand.getBrandId();
        newCar.model = brandModel.getBrandModelId();
        newCar.color = etCarColor.getText().toString();

        newCar.number = etCarNumber.getText().toString();

        final String driverLicense = etDriverLicense.getText().toString();
        String year = etCarYear.getText().toString();

        if (driverLicense.length() < 6) {
            etDriverLicense.setError("Минимально 6 символа");
            etDriverLicense.requestFocus();
            createCarCreateAnalyticsEvent("Серия и номер прав: Минимально 6 символа");
            return;
        }


        if (newCar.number.length() < 6 || newCar.number.length() > 10) {
            etCarNumber.setError("Неверно задано");
            etCarNumber.requestFocus();
            createCarCreateAnalyticsEvent("Номер автомобиля: Неверно задано");
            return;
        }

        if (!Helper.isYearValid(year)) {
            etCarYear.setError("Неверно задано");
            etCarYear.requestFocus();
            createCarCreateAnalyticsEvent("Год машины: Неверно задано");
            return;
        }

        newCar.year = Integer.parseInt(etCarYear.getText().toString());

        if (newCar.color.length() < 3){
            etCarColor.setError("Не менее 3 символов");
            etCarColor.requestFocus();
            createCarCreateAnalyticsEvent("Цвет: Не менее 3 символов");
            return;
        }

        if (newCar.color.length() > 20) {
            etCarColor.setError("Не более 20 символов");
            etCarColor.requestFocus();
            createCarCreateAnalyticsEvent("Цвет: Не более 20 символов");
            return;
        }

        showProgress("Сохранение");

        Callback<NCar> callback = new Callback<NCar>() {
            @Override
            public void success(NCar nCar, Response response) {
                updateCurrentUserCar(nCar);
                if( !mUser.getDriverLicenseNumber().equals(driverLicense) ){
                    updateDriverLicense(driverLicense);
                }else {
                    hideProgress();
                    finishUpdate();
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
        };
        if(isNew){
            RestClient.getCarService().addCar(newCar, callback);
        }else{
            RestClient.getCarService().updateCar(newCar.id, newCar, callback);
        }

    }

    private void updateCurrentUserCar(NCar nCar){
        mCar = new Car(nCar);
        ArrayList<Car> cars = new ArrayList<>();
        cars.add(mCar);
        mUser.setCars(cars);
        GlobalSingleton.getInstance().currentUser = mUser;
    }

    private void updateDriverLicense(String driverLicense){
        mUser.setDriverLicenseNumber(driverLicense);
        RestClient.getUserService().save(mUser.getId(), mUser, new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                mUser = user;
                GlobalSingleton.getInstance().currentUser = mUser;
                hideProgress();
                finishUpdate();
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.getKind() == RetrofitError.Kind.HTTP) {
                    Toast.makeText(getActivity(), getString(R.string.error_an_error_has_occurred_try_again), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(), getString(R.string.error_could_not_connect_to_server), Toast.LENGTH_SHORT).show();
                }
                hideProgress();
            }
        });
    }

    private void finishUpdate(){
        if(isNew){
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra("finish", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
            startActivity(intent);
            getActivity().finish();
        }else{
            Toast.makeText(getActivity(), "Сохранено", Toast.LENGTH_LONG).show();
        }
    }

    private void fetchBrands(final boolean setSelection){
        showProgress("Загрузка");
        if(Brand.isBrandsUpToDate()){
            fillBrandSpinner(Brand.getAll(), setSelection);
        }else {
            RestClient.getCarService().getAllCarBrands(new Callback<ArrayList<Brand>>() {
                @Override
                public void success(ArrayList<Brand> brands, Response response) {
                    Brand.upgradeBrands(brands);
                    fillBrandSpinner(brands, setSelection);
                }

                @Override
                public void failure(RetrofitError error) {
                    if (error.getKind() == RetrofitError.Kind.HTTP) {
                        Toast.makeText(getActivity(), getString(R.string.error_an_error_has_occurred_try_again), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getActivity(), getString(R.string.error_could_not_connect_to_server), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void fillBrandSpinner(ArrayList<Brand> brands, boolean setSelection){
        brandArrayAdapter.clear();
        brandArrayAdapter.addAll(brands);
        brandArrayAdapter.notifyDataSetChanged();

        if (setSelection && mCar != null) {
            Brand brand = mCar.getBrand();
            if (brand != null) {
                int position = brandArrayAdapter.getPosition(brand);
                carBrandSpinner.setSelection(position, false);

                fetchBrandModels(true);
                carBrandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        fetchBrandModels(false);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        }else{
            fetchBrandModels(false);
        }
        hideProgress();
    }

    private void fetchBrandModels(final boolean setSelection){
        showProgress("Загрузка");
        Brand selectedBrand = (Brand) carBrandSpinner.getSelectedItem();
        final Brand dbBrand = Brand.getByBrandId(selectedBrand.getBrandId());

        if(dbBrand.isBrandModelsUpToDate()){
            fillBrandModelSpinner(dbBrand.getBrandModels(), setSelection);
        }else {
            RestClient.getCarService().getCarModelByBrandId(selectedBrand.getBrandId(), new Callback<ArrayList<BrandModel>>() {
                @Override
                public void success(ArrayList<BrandModel> brandModels, Response response) {
                    dbBrand.upgradeBrandModels(brandModels);
                    fillBrandModelSpinner(brandModels, setSelection);
                }

                @Override
                public void failure(RetrofitError error) {
                    if (error.getKind() == RetrofitError.Kind.HTTP) {
                        Toast.makeText(getActivity(), getString(R.string.error_an_error_has_occurred_try_again), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getActivity(), getString(R.string.error_could_not_connect_to_server), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void fillBrandModelSpinner(ArrayList<BrandModel> brandModels, boolean setSelection){
        brandModelArrayAdapter.clear();
        brandModelArrayAdapter.addAll(brandModels);
        brandModelArrayAdapter.notifyDataSetChanged();

        if (setSelection && mCar != null) {
            BrandModel brandModel = mCar.getModel();
            if (brandModel != null) {
                int position = brandModelArrayAdapter.getPosition(brandModel);
                carModelSpinner.setSelection(position);
            }
        }
        hideProgress();
    }
}
