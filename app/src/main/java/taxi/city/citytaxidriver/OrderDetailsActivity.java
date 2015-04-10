package taxi.city.citytaxidriver;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import taxi.city.citytaxidriver.Core.Order;
import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Enums.OrderStatus;
import taxi.city.citytaxidriver.Service.ApiService;


public class OrderDetailsActivity extends ActionBarActivity {

    private static final int ACCEPT_ORDER = 1;
    private static final int WAIT_ORDER = 2;
    private static final int START_ORDER = 3;

    TextView tvAddress;
    TextView tvDescription;
    TextView tvClientPhone;
    EditText etDeclineReason;

    SendPostRequestTask sendTask;
    Order order;
    User user;
    ApiService api;
    int orderType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        Intent intent = getIntent();
        int type = intent.getIntExtra("type", 0);

        orderType = type;
        Initialize(type);
    }

    private void Initialize(int type) {
        order = Order.getInstance();
        user = User.getInstance();
        api = ApiService.getInstance();

        tvAddress = (TextView) findViewById(R.id.textViewStartAddress);
        tvDescription = (TextView) findViewById(R.id.textViewDescription);
        tvClientPhone = (TextView) findViewById(R.id.textViewClientPhone);
        etDeclineReason = (EditText) findViewById(R.id.editTextDeclineReason);

        LinearLayout llTop = (LinearLayout) findViewById(R.id.linearLayoutButtonGroupMap);
        LinearLayout llOkCancel = (LinearLayout) findViewById(R.id.linearLayoutButtonGroupOkCancel);

        Button btnTake = (Button) findViewById(R.id.buttonTakeOrder);
        Button btnCancel = (Button) findViewById(R.id.buttonCancelOrder);
        Button btnMap = (Button) findViewById(R.id.buttonMap);

        tvAddress.setText(order.addressStart);
        tvClientPhone.setText(order.clientPhone);
        tvDescription.setText(order.description);

        if (type == ACCEPT_ORDER) {
            llTop.setVisibility(View.GONE);
            btnTake.setText("Взять");
            btnCancel.setText("Назад");
            tvClientPhone.setVisibility(View.VISIBLE);
            etDeclineReason.setVisibility(View.GONE);
        } else if (type == WAIT_ORDER) {
            llTop.setVisibility(View.VISIBLE);
            btnTake.setText("На месте");
            btnCancel.setText("Отказ");
            btnMap.setText("На карте");
            tvClientPhone.setVisibility(View.VISIBLE);
            etDeclineReason.setVisibility(View.GONE);
        } else if (type == START_ORDER) {
            llTop.setVisibility(View.GONE);
            btnTake.setText("На борту");
            btnCancel.setText("Отказ");
            tvClientPhone.setVisibility(View.VISIBLE);
            etDeclineReason.setVisibility(View.GONE);
        }

        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orderType == ACCEPT_ORDER) {
                    SendPostRequest(OrderStatus.STATUS.ACCEPTED);
                } else if (orderType == WAIT_ORDER) {
                    SendPostRequest(OrderStatus.STATUS.WAITING);
                } else if (orderType == START_ORDER) {
                    SendPostRequest(OrderStatus.STATUS.ONTHEWAY);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orderType == ACCEPT_ORDER) {
                    Intent intent = new Intent();
                    intent.putExtra("returnCode", false);
                    setResult(1, intent);
                    finish();
                }
                if (orderType == WAIT_ORDER) {
                    SendPostRequest(OrderStatus.STATUS.NEW);
                }
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("returnCode", false);
                setResult(1, intent);
                finish();
            }
        });
    }

    private void SendPostRequest(OrderStatus.STATUS status) {
        if (sendTask != null) {
            return;
        }

        sendTask = new SendPostRequestTask(status);
        sendTask.execute((Void) null);
    }

    private class SendPostRequestTask extends AsyncTask<Void, Void, JSONObject> {
        SendPostRequestTask(OrderStatus.STATUS type) {
            order.status = type;
            order.driver = user.id;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.

            JSONObject data = new JSONObject();
            try {
                data.put("status", order.status);
                if (order.status != OrderStatus.STATUS.NEW) {
                    data.put("driver", order.driver);
                } else {
                    data.put("driver", null);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return api.patchRequest(data, "orders/" + order.id + "/");
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            sendTask = null;
            try {
                if (result != null && result.getInt("status_code") == HttpStatus.SC_OK) {
                    if (order.status != OrderStatus.STATUS.NEW) {
                        Toast.makeText(getApplicationContext(), "Заказ обновлён", Toast.LENGTH_LONG).show();
                        FinishTakeOrder();
                    } else {
                        Toast.makeText(getApplicationContext(), "Заказ отменён", Toast.LENGTH_LONG).show();
                        order.clear();
                        FinishCancelOrder();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Ошибка при отправке на сервер", Toast.LENGTH_LONG).show();
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
        if (orderType == ACCEPT_ORDER) {
            Intent intent = new Intent();
            intent.putExtra("returnCode", true);
            setResult(1, intent);
            finish();
        }
        if (orderType == WAIT_ORDER) {
            Intent intent = new Intent();
            intent.putExtra("returnCode", true);
            setResult(3, intent);
            finish();
        }
    }

    private void FinishCancelOrder() {
        if (orderType == WAIT_ORDER) {
            Intent intent = new Intent();
            intent.putExtra("returnCode", false);
            setResult(3, intent);
            finish();
        }
    }
}