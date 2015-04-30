package taxi.city.citytaxidriver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import taxi.city.citytaxidriver.Core.Client;
import taxi.city.citytaxidriver.Core.GlobalParameters;
import taxi.city.citytaxidriver.Core.Order;
import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Enums.OStatus;
import taxi.city.citytaxidriver.Service.ApiService;
import taxi.city.citytaxidriver.Utils.Helper;


public class OrderDetailsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_activity);

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
        private boolean isActive;
        private ApiService api = ApiService.getInstance();
        private Order order = Order.getInstance();
        private User user = User.getInstance();
        private GlobalParameters gp = GlobalParameters.getInstance();
        private SendPostRequestTask mTask = null;

        TextView tvClientPhone;
        TextView tvClientPhoneLabel;
        ImageButton imgBtnCallClient;

        Button btnOk;
        Button btnCancel;
        Button btnMap;

        LinearLayout llBtnMap;

        public OrderDetailsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_order_details_activity, container, false);
            mClient = (Client)getActivity().getIntent().getSerializableExtra("DATA");
            isActive = getActivity().getIntent().getBooleanExtra("ACTIVE", false);

            TextView tvAddressStart = (TextView) rootView.findViewById(R.id.textViewStartAddress);
            tvClientPhone = (TextView) rootView.findViewById(R.id.textViewClientPhone);
            tvClientPhoneLabel = (TextView) rootView.findViewById(R.id.textViewClientPhoneLabel);
            TextView tvAddressStop = (TextView) rootView.findViewById(R.id.textViewStopAddress);
            TextView tvDescription = (TextView) rootView.findViewById(R.id.textViewDescription);
            TextView tvFixedPrice = (TextView) rootView.findViewById(R.id.textViewFixedPrice);
            LinearLayout llFixedPrice = (LinearLayout) rootView.findViewById(R.id.linearLayoutFixedPrice);
            llBtnMap = (LinearLayout) rootView.findViewById(R.id.linearLayoutMapInfo);
            imgBtnCallClient = (ImageButton) rootView.findViewById(R.id.imageButtonCallClient);

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
                tvClientPhone.setVisibility(View.GONE);
                tvClientPhoneLabel.setVisibility(View.GONE);
                imgBtnCallClient.setVisibility(View.GONE);
                btnCancel.setBackgroundResource(R.drawable.button_shape_yellow);
            } else if (mClient.status.equals(OStatus.ACCEPTED.toString())) {
                llBtnMap.setVisibility(View.VISIBLE);
                btnOk.setText("На месте");
                btnCancel.setText("Отказ");
                tvClientPhone.setVisibility(View.VISIBLE);
                tvClientPhoneLabel.setVisibility(View.VISIBLE);
                imgBtnCallClient.setVisibility(View.VISIBLE);
                btnCancel.setBackgroundResource(R.drawable.button_shape_red);
            } else {
                llBtnMap.setVisibility(View.VISIBLE);
                tvClientPhone.setVisibility(View.VISIBLE);
                tvClientPhoneLabel.setVisibility(View.VISIBLE);
                imgBtnCallClient.setVisibility(View.VISIBLE);
                btnOk.setText("На борту");
                btnCancel.setText("Отказ");
                btnCancel.setBackgroundResource(R.drawable.button_shape_red);
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonActionOk:
                    if (mClient.status.equals(OStatus.NEW.toString())) {
                        SendPostRequest(OStatus.ACCEPTED);
                    } else if (mClient.status.equals(OStatus.ACCEPTED.toString())) {
                        mClient.status = OStatus.PENDING.toString();
                        order.status = OStatus.PENDING;
                        SendPostRequest(OStatus.PENDING);
                    } else if (mClient.status.equals(OStatus.PENDING.toString())) {
                        mClient.status = OStatus.ONTHEWAY.toString();
                        order.status = OStatus.ONTHEWAY;
                        SendPostRequest(OStatus.ONTHEWAY);
                    }
                    break;
                case R.id.buttonActionCancel:
                    if (!mClient.status.equals(OStatus.NEW.toString())) cancelOrder();
                    break;
                case R.id.imageButtonCallClient:
                    callClient();
                    break;
                default:
                    Intent intent = new Intent();
                    intent.putExtra("returnCode", true);
                    getActivity().setResult(isActive ? 3 : 1, intent);
                    getActivity().finish();
            }
        }

        private void callClient() {
            final AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());
            //final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
            final String message = "Вы уверены что хотите позвонить?";
            final String title = order.clientPhone;

            builder.setMessage(message)
                    .setTitle(title)
                    .setPositiveButton("Позвонить",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                                    callIntent.setData(Uri.parse("tel:" + mClient.phone));
                                    startActivity(callIntent);
                                    d.dismiss();
                                }
                            })
                    .setNegativeButton("Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                }
                            });
            builder.create().show();
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
                        else if (result.getString("status").equals(OStatus.ACCEPTED.toString())) {
                            mClient.status = OStatus.ACCEPTED.toString();
                            Helper.setOrder(result);
                        } else if (result.getString("status").equals(OStatus.NEW.toString())) {
                            order.clear();
                            Intent intent = new Intent();
                            intent.putExtra("returnCode", false);
                            getActivity().setResult(isActive ? 3 : 1, intent);
                            getActivity().finish();
                        }
                        updateViews();
                    }
                    if (order.status == OStatus.ONTHEWAY) {
                        Intent intent = new Intent();
                        intent.putExtra("returnCode", true);
                        if (!isActive)
                            getActivity().setResult(1, intent);
                        else
                            getActivity().setResult(3, intent);
                        getActivity().finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {

                }
                updateViews();
            }

            @Override
            protected void onCancelled() {
                mTask = null;
            }
        }
    }
}
