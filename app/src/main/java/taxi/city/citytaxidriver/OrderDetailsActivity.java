package taxi.city.citytaxidriver;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import taxi.city.citytaxidriver.Core.Client;
import taxi.city.citytaxidriver.Core.Order;
import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Enums.OStatus;
import taxi.city.citytaxidriver.Service.ApiService;
import taxi.city.citytaxidriver.Utils.Helper;


public class OrderDetailsActivity extends ActionBarActivity {

    private static final int NEW = 1;
    private static final int FINISHED = 2;
    private static final int ACTIVE = 3;

    TextView tvAddress;
    TextView tvDescription;
    TextView tvClientPhone;
    TextView tvSum;
    TextView tvDistance;
    TextView tvTravelTime;
    TextView tvWaitTime;
    TextView tvWaitSum;
    TextView tvTotalSum;

    SendPostRequestTask sendTask;
    Order order;
    User user;
    ApiService api;
    int orderType;
    Client mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        Intent intent = getIntent();
        Client client = (Client)intent.getExtras().getSerializable("data");
        int type = 0;
        if (client != null) {
            switch (client.status) {
                case "new":
                    type = NEW;
                    break;
                case "finished":
                    type = FINISHED;
                    break;
                default:
                    type = ACTIVE;
                    break;
            }
        }

        orderType = type;
        mClient = client;
        Initialize(type);
    }

    private void Initialize(int type) {
        order = Order.getInstance();
        user = User.getInstance();
        api = ApiService.getInstance();

        tvAddress = (TextView) findViewById(R.id.textViewStartAddress);
        tvDescription = (TextView) findViewById(R.id.textViewDescription);
        tvClientPhone = (TextView) findViewById(R.id.textViewClientPhone);
        tvSum = (TextView) findViewById(R.id.textViewOrderTravelSum);
        tvDistance = (TextView) findViewById(R.id.textViewOrderDistance);
        tvTravelTime = (TextView) findViewById(R.id.textViewOrderTime);
        tvWaitTime = (TextView) findViewById(R.id.textViewOrderWaitTime);
        tvWaitSum = (TextView) findViewById(R.id.textViewOrderWaitSum);
        tvTotalSum = (TextView) findViewById(R.id.textViewOrderTotalSum);

        LinearLayout llDescription = (LinearLayout) findViewById(R.id.linearLayoutOrderDetails);

        Button btnTake = (Button) findViewById(R.id.buttonTakeOrder);
        Button btnCancel = (Button) findViewById(R.id.buttonCancelOrder);

        tvAddress.setText(mClient.addressStart);
        tvClientPhone.setText(mClient.phone);
        double totalSum = 0;
        //if (mClient.addressEnd != null && !mClient.addressEnd.equals("null"))
        if (mClient.description != null)
            tvDescription.setText(mClient.description);
        if (mClient.sum != null) {
            totalSum += Double.valueOf(mClient.sum);
            tvSum.setText("Сумма: " + mClient.sum + " сом");
        }
        if (mClient.distance != null)
            tvDistance.setText("Путь: " + mClient.distance + " км");
        if (mClient.time != null)
            tvTravelTime.setText("Время: " + mClient.time);
        if (mClient.waitTime != null)
            tvWaitTime.setText("Время ожидания: " + mClient.waitTime);
        if (mClient.waitSum != null) {
            try {
                totalSum += Double.valueOf(mClient.waitSum);
                tvWaitSum.setText("Сумма ожидания: " + mClient.waitSum + " сом");
            }
            catch (Exception e) {

            }
        }

        tvTotalSum.setText("Общая сумма: " + totalSum + " сом");

        if (type == NEW) {
            btnTake.setText("Взять");
            btnCancel.setText("Назад");
            tvClientPhone.setVisibility(View.GONE);
            llDescription.setVisibility(View.GONE);
        } else if (type == FINISHED) {
            llDescription.setVisibility(View.VISIBLE);
            btnCancel.setText("Назад");
            btnTake.setVisibility(View.GONE);
            tvClientPhone.setVisibility(View.GONE);
        } else if (type == ACTIVE) {
            llDescription.setVisibility(View.VISIBLE);
            btnTake.setVisibility(View.GONE);
            btnCancel.setText("Назад");
            tvClientPhone.setVisibility(View.VISIBLE);
        }

        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orderType == NEW) {
                    order.clear();
                    Helper.setOrder(mClient);
                    SendPostRequest(OStatus.ACCEPTED);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orderType == NEW) {
                    Intent intent = new Intent();
                    intent.putExtra("returnCode", false);
                    setResult(1, intent);
                    finish();
                } else {
                    finish();
                }
            }
        });
    }

    private void SendPostRequest(OStatus status) {
        if (sendTask != null) {
            return;
        }

        sendTask = new SendPostRequestTask(status);
        sendTask.execute((Void) null);
    }

    private class SendPostRequestTask extends AsyncTask<Void, Void, JSONObject> {
        SendPostRequestTask(OStatus type) {
            order.status = type;
            order.driver = user.id;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.

            JSONObject res = new JSONObject();
            JSONObject data = new JSONObject();
            try {
                data.put("status", order.status);
                data.put("driver", order.driver);
                if (order.endPoint != null) {
                    data.put("address_stop", Helper.getFormattedLatLng(order.endPoint));
                }
                JSONObject object = api.getOrderRequest(null, "orders/" + order.id + "/");
                if (object != null && object.getInt("status_code") == HttpStatus.SC_OK
                            && object.getString("status").equals(OStatus.NEW.toString())) {
                    res = api.patchRequest(data, "orders/" + order.id + "/");
                } else {
                    res = null;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                res = null;
            }

            return res;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            sendTask = null;
            try {
                if (result != null && result.getInt("status_code") == HttpStatus.SC_OK) {
                    Toast.makeText(getApplicationContext(), "Заказ обновлён", Toast.LENGTH_LONG).show();
                    FinishTakeOrder();
                } else {
                    Toast.makeText(getApplicationContext(), "Заказ уже занят", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Ошибка при отправке на сервер", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            sendTask = null;
        }
    }

    private void FinishTakeOrder() {
        Helper.setOrder(mClient);
        Intent intent = new Intent();
        intent.putExtra("returnCode", true);
        setResult(1, intent);
        finish();
    }
}