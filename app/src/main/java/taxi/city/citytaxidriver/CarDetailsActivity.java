package taxi.city.citytaxidriver;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Service.ApiService;


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
        private CarUpdateTask mTask = null;

        Spinner carBrandSpinner;
        Spinner carModelSpinner;
        Spinner carColorSpinner;
        EditText etCarColor;
        EditText etTechPassport;
        EditText etCarNumber;
        EditText etPassportNumber;
        EditText etDriverLicense;
        Button btnBack;
        Button btnSave;
        Button btnExit;

        public CarDetailsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_car_details, container, false);

            carBrandSpinner = (Spinner) rootView.findViewById(R.id.spinnerCarBrand);
            carModelSpinner = (Spinner) rootView.findViewById(R.id.spinnerCarModel);
            etCarColor = (EditText) rootView.findViewById(R.id.spinnerCarColor);
            etTechPassport = (EditText) rootView.findViewById(R.id.editTextTechPassport);
            etDriverLicense = (EditText) rootView.findViewById(R.id.editTextDriverLicense);
            etCarNumber = (EditText) rootView.findViewById(R.id.editTextCarNumber);
            etPassportNumber = (EditText)rootView.findViewById(R.id.editTextPassportNumber);

            btnBack.setOnClickListener(this);
            btnExit.setOnClickListener(this);
            btnSave.setOnClickListener(this);

            updateTask(false);

            return rootView;
        }

        private void updateTask(boolean update) {
            if (mTask != null)
                return;

            mTask = new CarUpdateTask(update);
            mTask.execute((Void) null);
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
            private JSONObject json = new JSONObject();
            private boolean isUpdate = false;

            CarUpdateTask(boolean update) {
                isUpdate = update;
            }

            @Override
            protected JSONObject doInBackground(Void... params) {
                // TODO: attempt authentication against a network service.
                // Simulate network access.

                if (isUpdate) {
                    return ApiService.getInstance().patchRequest(json, "userscars/");
                } else {
                    return ApiService.getInstance().getDataFromGetRequest(null, "usercars/?driver=" + User.getInstance() + "/");
                }
            }

            @Override
            protected void onPostExecute(final JSONObject result) {
                mTask = null;
                int statusCode = -1;
                try {
                    if(result != null && result.has("status_code")) {
                        statusCode = result.getInt("status_code");
                    }
                    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_ACCEPTED || statusCode == HttpStatus.SC_CREATED) {
                        fillForms(result.getJSONArray("result"));
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

        private void fillForms(JSONArray array) throws JSONException{
            if (array.length() < 1) return;
            JSONObject object = array.getJSONObject(0);
            etCarColor.setText(object.has("color") ? object.getString("color") : null);
            etTechPassport.setText(object.has("technical_certificate") ? object.getString("technical_certificate") : null);
            etCarNumber.setText(object.has("car_number") ? object.getString("car_number") : null);
            etPassportNumber.setText("");
            etDriverLicense.setText("");
        }
    }
}
