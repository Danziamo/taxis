package taxi.city.citytaxidriver;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Service.ApiService;
import taxi.city.citytaxidriver.Utils.Helper;


public class AccountActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new AccountFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class AccountFragment extends Fragment {

        TextView tvAccountNumber;
        TextView tvAccountBalance;
        private FetchAccountTask mTask = null;

        public AccountFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_account, container, false);

            tvAccountNumber = (TextView)rootView.findViewById(R.id.textViewAccountNumber);
            tvAccountBalance = (TextView) rootView.findViewById(R.id.textViewAccountBalance);

            Button buttonBack = (Button)rootView.findViewById(R.id.buttonBack);
            buttonBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });

            fetchTask();
            return rootView;
        }

        private void fetchTask(){
            if (mTask != null) return;

            mTask = new FetchAccountTask();
            mTask.execute((Void) null);
        }

        private class FetchAccountTask extends AsyncTask<Void, Void, JSONObject> {

            FetchAccountTask() {}

            @Override
            protected JSONObject doInBackground(Void... params) {
                return ApiService.getInstance().getOrderRequest(null, "users/" + User.getInstance().id + "/");
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
                        fillForms(result);
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

        private void fillForms(JSONObject object) throws JSONException{
            String balance = object.getString("balance");
            double b = balance == null ? 0 : Double.valueOf(balance);
            tvAccountBalance.setText(String.valueOf((int)b) + "  сом");
            tvAccountNumber.setText(User.getInstance().phone);
            User.getInstance().balance = b;
        }
    }
}
