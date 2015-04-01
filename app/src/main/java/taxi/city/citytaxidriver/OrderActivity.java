package taxi.city.citytaxidriver;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import taxi.city.citytaxidriver.Core.Client;
import taxi.city.citytaxidriver.Core.ClientAdapter;
import taxi.city.citytaxidriver.Core.Order;
import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Enums.OrderStatus;
import taxi.city.citytaxidriver.Service.ApiService;


public class OrderActivity extends ActionBarActivity {

    private String TAG = "OrderActivity";
    private ArrayList<Client> list = new ArrayList<>();
    private Order order = Order.getInstance();
    private ApiService api = ApiService.getInstance();
    private User user = User.getInstance();
    private FetchOrderTask mFetchTask = null;
    private SendPostRequestTask sendTask = null;
    ListView lvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        lvMain = (ListView) findViewById(R.id.orderList);
        fetchData();
    }

    private LatLng getLatLng(String s) {
        String[] geo = s.replace("(", "").replace(")", "").split(" ");

        double latitude = Double.valueOf(geo[1].trim());
        double longitude = Double.valueOf(geo[2].trim());
        return new LatLng(latitude, longitude);
    }

    private void InitListView(Map.Entry map) {
        list.clear();
        try {
            Log.d(TAG, "Start to fill up list view");
            if (map != null && (int)map.getKey() == 200) {
                JSONArray arr = (JSONArray)map.getValue();
                for (int i=0; i < arr.length(); ++i) {
                    JSONObject row = arr.getJSONObject(i);
                    if (row.getString("status").equals("new")) {
                        Client client = new Client();
                        client.phone = row.getString("client_phone");
                        client.startPoint = getLatLng(row.getString("address_start"));
                        client.endPoint = getLatLng(row.getString("address_stop"));
                        client.driver = user.id;
                        client.id = row.getInt("id");
                        client.waitTime = row.getString("wait_time");
                        client.tariff = row.getInt("tariff");
                        client.status = row.getString("status");
                        client.orderTime = row.getString("order_time");
                        list.add(client);
                    }
                }
                ClientAdapter adapter = new ClientAdapter(OrderActivity.this, list);
                lvMain.setAdapter(adapter);
                lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView idTextView = (TextView)findViewById(R.id.orderId);
                        TextView clientPhoneTextView = (TextView)findViewById(R.id.orderPhone);
                        int orderId = Integer.valueOf(idTextView.getText().toString());
                        String clientPhone = clientPhoneTextView.getText().toString();
                        order.id = orderId;
                        order.clientPhone = clientPhone;
                        for (int i = list.size() - 1; i >= 0; i -= 1) {
                            if (orderId == list.get(i).id) {
                                order.setOrder(list.get(i));
                                break;
                            }
                        }
                        SendPostRequest(OrderStatus.STATUS.ACCEPTED);
                        Intent intent = new Intent();
                        intent.putExtra("clientLocation", order.startPoint);
                        setResult(1, intent);
                        finish();
                    }
                });
                Log.d(TAG, "Finished filling up listview");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Oops something happened while initializing listview");
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_order:
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void fetchData() {
        Log.d(TAG, "Starting fetching data");
        if (mFetchTask != null) {
            return;
        }

        mFetchTask = new FetchOrderTask();
        mFetchTask.execute((Void) null);

    }

    public class FetchOrderTask extends AsyncTask<Void, Void, Map.Entry>{

        FetchOrderTask() {}

        @Override
        protected Map.Entry doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.

            return api.getDataFromGetRequest(null, "orders/");
        }

        @Override
        protected void onPostExecute(Map.Entry map) {
            mFetchTask = null;
            Log.d(TAG, "Finished fetching data " + map.toString());
            if ((int) map.getKey() == HttpStatus.SC_OK)
            {
                InitListView(map);
            } else {
                Toast.makeText(getApplicationContext(), "Сервис недоступен", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mFetchTask = null;
        }
    }

    private void SendPostRequest(OrderStatus.STATUS status) {
        if (sendTask != null) {
            return;
        }

        sendTask = new SendPostRequestTask(status);
        sendTask.execute((Void) null);
    }

    private class SendPostRequestTask extends AsyncTask<Void, Void, Map.Entry> {
        SendPostRequestTask(OrderStatus.STATUS type) {
            order.status = type;
            order.driver = user.id;
        }

        @Override
        protected Map.Entry doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.

            JSONObject data = new JSONObject();
            try {
                data = order.getOrderAsJson();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return api.putDataRequest(data, "orders/" + order.id + "/");
        }

        @Override
        protected void onPostExecute(Map.Entry map) {
            if ((int) map.getKey() == HttpStatus.SC_OK)
            {
                sendTask = null;
                Toast.makeText(getApplicationContext(), "Заказ обновлён", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Ошибка при отправке на сервер", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            sendTask = null;
        }
    }

}
