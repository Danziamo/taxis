package taxi.city.citytaxidriver;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import taxi.city.citytaxidriver.Core.Client;
import taxi.city.citytaxidriver.Core.GlobalParameters;
import taxi.city.citytaxidriver.Core.Order;
import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Enums.OStatus;
import taxi.city.citytaxidriver.Service.ApiService;
import taxi.city.citytaxidriver.Utils.Helper;


public class OrderDetailsActivity2 extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_activity2);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new OrderDetailsFragment())
                    .commit();
        }
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class OrderDetailsFragment extends Fragment implements View.OnClickListener{
        private Client mClient;
        private ApiService api = ApiService.getInstance();
        private Order order = Order.getInstance();
        private User user = User.getInstance();
        private GlobalParameters gp = GlobalParameters.getInstance();
        private SendPostRequestTask mTask = null;

        Button btnOk;
        Button btnCancel;
        Button btnMap;

        LinearLayout llBtnMap;

        public OrderDetailsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_order_details_activity2, container, false);
            mClient = (Client)getActivity().getIntent().getSerializableExtra("DATA");

            TextView tvAddressStart = (TextView) rootView.findViewById(R.id.textViewStartAddress);
            TextView tvClientPhone = (TextView) rootView.findViewById(R.id.textViewClientPhone);
            TextView tvAddressStop = (TextView) rootView.findViewById(R.id.textViewStopAddress);
            TextView tvDescription = (TextView) rootView.findViewById(R.id.textViewDescription);
            TextView tvFixedPrice = (TextView) rootView.findViewById(R.id.textViewFixedPrice);
            LinearLayout llFixedPrice = (LinearLayout) rootView.findViewById(R.id.linearLayoutFixedPrice);
            llBtnMap = (LinearLayout) rootView.findViewById(R.id.linearLayoutMapInfo);

            tvAddressStart.setText(mClient.addressStart);
            tvClientPhone.setText(mClient.phone);
            tvAddressStop.setText(mClient.addressEnd);
            tvDescription.setText(mClient.description);
            double fixedPrice = Helper.getDouble(mClient.fixedPrice);
            tvFixedPrice.setText((int)fixedPrice + " сом");
            if (fixedPrice < 50) llFixedPrice.setVisibility(View.GONE);

            btnOk = (Button) rootView.findViewById(R.id.buttonActionOk);
            btnCancel = (Button) rootView.findViewById(R.id.buttonActionCancel);
            btnMap = (Button) rootView.findViewById(R.id.buttonMapInfo);
            btnOk.setOnClickListener(this);
            btnCancel.setOnClickListener(this);
            btnMap.setOnClickListener(this);

            updateViews();

            return rootView;
        }

        private void updateViews() {
            if (mClient.status.equals(OStatus.NEW.toString())) {
                llBtnMap.setVisibility(View.GONE);
                btnOk.setText("Взять");
                btnCancel.setText("Заказы");
                btnCancel.setBackgroundResource(R.drawable.button_shape_yellow);
            } else if (mClient.status.equals(OStatus.ACCEPTED.toString())) {
                llBtnMap.setVisibility(View.VISIBLE);
                btnOk.setText("На месте");
                btnCancel.setText("Отказ");
                btnCancel.setBackgroundResource(R.drawable.button_shape_red);
            } else {
                llBtnMap.setVisibility(View.VISIBLE);
                btnOk.setText("На борту");
                btnCancel.setText("Отказ");
                btnCancel.setBackgroundColor(R.drawable.button_shape_red);
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonActionOk:
                    if (mClient.status.equals(OStatus.NEW.toString())) {
                        SendPostRequest(OStatus.ACCEPTED);
                    } else if (mClient.status.equals(OStatus.ACCEPTED.toString())) {
                        SendPostRequest(OStatus.PENDING);
                    } else if (mClient.status.equals(OStatus.PENDING.toString())) {
                        SendPostRequest(OStatus.ONTHEWAY);
                    }
                    break;
                case R.id.buttonActionCancel:
                    if (!mClient.status.equals(OStatus.NEW.toString())) cancelOrder();
                    getActivity().finish();
                    break;
                default:
                    getActivity().finish();
            }
        }

        private void cancelOrder() {
            SendPostRequest(OStatus.NEW);
        }

        private void SendPostRequest(OStatus status) {
            if (mTask != null) {
                return;
            }

            mTask = new SendPostRequestTask(status, mClient.id);
            mTask.execute((Void) null);
        }

        private class SendPostRequestTask extends AsyncTask<Void, Void, JSONObject> {
            private String mDriver;
            private String mStatus;
            private String mCurrPosition;
            private String mId;

            SendPostRequestTask(OStatus type, int orderId) {
                mStatus = type.toString();
                mDriver = type == OStatus.NEW ? null : String.valueOf(user.id);
                mCurrPosition = gp.getPosition();
                mId = orderId == 0 ? null : String.valueOf(orderId);
            }

            @Override
            protected JSONObject doInBackground(Void... params) {
                JSONObject res = new JSONObject();
                JSONObject data = new JSONObject();
                try {
                    data.put("status", mStatus);
                    data.put("driver", mDriver == null ? JSONObject.NULL : mDriver);
                    if (mCurrPosition != null) data.put("address_stop", mCurrPosition);

                    if (mClient.status.equals(OStatus.NEW.toString())) {
                        JSONObject object = api.getOrderRequest(null, "orders/" + mId + "/");
                        if (Helper.isSuccess(object) && !object.getString("status").equals(OStatus.NEW.toString())) {
                            Toast.makeText(getActivity(), "Заказ невозможно взять", Toast.LENGTH_LONG).show();
                            return null;
                        }
                    }

                    res = api.patchRequest(data, "orders/" + mId + "/");
                } catch (JSONException e) {
                    e.printStackTrace();
                    res = null;
                }
                return res;
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                mTask = null;
                try {
                    if (Helper.isSuccess(result)) {
                        Toast.makeText(getActivity(), "Заказ обновлён", Toast.LENGTH_LONG).show();
                        if (result.getString("status").equals(OStatus.CANCELED.toString())) order.clear();
                        updateViews();
                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {

                }
            }

            @Override
            protected void onCancelled() {
                mTask = null;
            }
        }
    }
}
