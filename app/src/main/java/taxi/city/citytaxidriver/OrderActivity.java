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

    private static final int ACCEPT_ORDER = 1;
    private static final int WAIT_ORDER = 2;
    private static final int START_ORDER = 3;

    private String TAG = "OrderActivity";
    private ArrayList<Client> list = new ArrayList<>();
    private Order order = Order.getInstance();
    private ApiService api = ApiService.getInstance();
    private User user = User.getInstance();
    private FetchOrderTask mFetchTask = null;
    ListView lvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        lvMain = (ListView) findViewById(R.id.orderList);
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*TextView idTextView = (TextView)parent.findViewById(R.id.orderId);
                //TextView clientAddressTextView = (TextView)findViewById(R.id.orderAddress);
                int orderId = Integer.valueOf(idTextView.getText().toString());
                //String clientAddress = clientAddressTextView.getText().toString();*/

                String text = ((TextView) view.findViewById(R.id.orderId)).getText().toString();
                int orderId = Integer.valueOf(text);
                order.id = orderId;

                for (int i = list.size() - 1; i >= 0; i -= 1) {
                    if (orderId == list.get(i).id) {
                        order.setOrder(list.get(i));
                        break;
                    }
                }
                goOrderDetails();
            }
        });
        fetchData();
    }

    private LatLng getLatLng(String s) {
        if (s == null || s.equals("null"))
            return null;
        String[] geo = s.replace("(", "").replace(")", "").split(" ");

        double latitude = Double.valueOf(geo[1].trim());
        double longitude = Double.valueOf(geo[2].trim());
        return new LatLng(latitude, longitude);
    }

    private void InitListView(JSONArray array) {
        list.clear();
        try {
            Log.d(TAG, "Start to fill up list view");
            for (int i=0; i < array.length(); ++i) {
                JSONObject row = array.getJSONObject(i);
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
                    client.addressStart = row.getString("address_start_name");
                    client.description = row.getString("description");
                    list.add(client);
                }
            }
            ClientAdapter adapter = new ClientAdapter(OrderActivity.this, list);
            lvMain.setAdapter(adapter);

            Log.d(TAG, "Finished filling up listview");
        } catch (JSONException e) {
            Log.e(TAG, "Oops something happened while initializing listview");
            e.printStackTrace();
        }

    }

    private void goOrderDetails() {
        Intent intent = new Intent(this, OrderDetailsActivity.class);
        intent.putExtra("type", ACCEPT_ORDER);
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
                order.clear();
            }
        }
    }


    /*@Override
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
    }*/

    private void fetchData() {
        Log.d(TAG, "Starting fetching data");
        if (mFetchTask != null) {
            return;
        }

        mFetchTask = new FetchOrderTask();
        mFetchTask.execute((Void) null);

    }

    public class FetchOrderTask extends AsyncTask<Void, Void, JSONObject>{

        FetchOrderTask() {}

        @Override
        protected JSONObject doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.

            return api.getDataFromGetRequest(null, "orders/?status=new");
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            mFetchTask = null;
            try {
                Log.d(TAG, "Finished fetching data " + result.toString());
                if (result.getInt("status_code") == HttpStatus.SC_OK) {
                    InitListView(result.getJSONArray("result"));
                } else {
                    Toast.makeText(getApplicationContext(), "Сервис недоступен", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            mFetchTask = null;
        }
    }
}
