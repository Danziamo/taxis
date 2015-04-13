package taxi.city.citytaxidriver;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import taxi.city.citytaxidriver.Core.Client;
import taxi.city.citytaxidriver.Core.ClientAdapter;
import taxi.city.citytaxidriver.Core.Order;
import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Service.ApiService;


public class OrderActivity extends ActionBarActivity {

    private String TAG = "OrderActivity";
    private ArrayList<Client> list = new ArrayList<>();
    private Order order = Order.getInstance();
    private ApiService api = ApiService.getInstance();
    private User user = User.getInstance();
    private FetchOrderTask mFetchTask = null;

    private Client client;
    ListView lvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        lvMain = (ListView) findViewById(R.id.orderList);
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String text = ((TextView) view.findViewById(R.id.orderId)).getText().toString();
                int orderId = Integer.valueOf(text);
                order.id = orderId;

                for (int i = list.size() - 1; i >= 0; i -= 1) {
                    if (orderId == list.get(i).id) {
                        client = list.get(i);
                        //order.setOrder(list.get(i));
                        break;
                    }
                }
                goOrderDetails(client);
            }
        });
        fetchData();
    }

    private void InitListView(JSONArray array) {
        list.clear();
        try {
            Log.d(TAG, "Start to fill up list view");
            for (int i=0; i < array.length(); ++i) {
                JSONObject row = array.getJSONObject(i);

                Client client = new Client();
                client.phone = row.getString("client_phone");
                client.startPoint = row.getString("address_start");
                client.endPoint = row.getString("address_stop");
                client.driver = user.id;
                client.id = row.getInt("id");
                client.waitTime = row.getString("wait_time");
                client.tariff = row.getInt("tariff");
                client.status = row.getString("status");
                client.orderTime = row.getString("order_time");
                client.addressStart = row.getString("address_start_name");
                client.description = row.getString("description");
                client.addressEnd = row.getString("address_stop_name");
                client.sum = row.getString("order_sum");
                client.distance = row.getString("order_distance");
                client.time = row.getString("order_travel_time");
                client.waitSum = row.getString("wait_time_price");
                list.add(client);

            }
            ClientAdapter adapter = new ClientAdapter(OrderActivity.this, list);
            lvMain.setAdapter(adapter);

            Log.d(TAG, "Finished filling up listview");
        } catch (JSONException e) {
            Log.e(TAG, "Oops something happened while initializing listview");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Cannot open order. Please re-login");
            e.printStackTrace();
        }

    }

    private void goOrderDetails(Client mClient) {
        Intent intent = new Intent(this, OrderDetailsActivity.class);
        intent.putExtra("data", mClient);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        order = Order.getInstance();
        if (requestCode == 1) {
            if (data != null && data.getBooleanExtra("returnCode", false)) {
                Intent intent = new Intent();
                intent.putExtra("returnCode", true);
                setResult(1, intent);
                finish();
            } else {
                //order.clear();
            }
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

    public class FetchOrderTask extends AsyncTask<Void, Void, JSONArray>{

        FetchOrderTask() {}

        @Override
        protected JSONArray doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.

            JSONArray array = new JSONArray();
            try {
                JSONObject result = api.getDataFromGetRequest(null, "orders/?status=new");
                if (result.getInt("status_code") == HttpStatus.SC_OK) {
                    JSONArray tempArray = result.getJSONArray("result");
                    for (int i = 0; i < tempArray.length(); ++i) {
                        array.put(tempArray.getJSONObject(i));
                    }
                }
                result = api.getDataFromGetRequest(null, "orders/?driver=" + user.id);
                if (result.getInt("status_code") == HttpStatus.SC_OK) {
                    JSONArray tempArray = result.getJSONArray("result");
                    for (int i = 0; i < tempArray.length(); ++i) {
                        array.put(tempArray.getJSONObject(i));
                    }
                }
                Log.d(TAG, "Finished fetching data " + result.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return array;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            mFetchTask = null;
            InitListView(result);
        }

        @Override
        protected void onCancelled() {
            mFetchTask = null;
        }
    }
}
