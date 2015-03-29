package taxi.city.citytaxidriver;

import android.app.Application;
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
import taxi.city.citytaxidriver.Service.ApiService;


public class OrderActivity extends ActionBarActivity {

    private ArrayList<Client> list = new ArrayList<>();
    private Order order = Order.getInstance();
    private ApiService api = ApiService.getInstance();
    private FetchOrderTask mFetchTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        fetchData();

        ListView lvMain = (ListView) findViewById(R.id.orderList);
        ClientAdapter adapter = new ClientAdapter(this, list);
        lvMain.setAdapter(adapter);
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView idTextView = (TextView)findViewById(R.id.orderId);
                TextView clientPhoneTextView = (TextView)findViewById(R.id.orderPhone);
                String orderId = idTextView.getText().toString();
                String clientPhone = clientPhoneTextView.getText().toString();
                order.id = Integer.valueOf(orderId);
                /*Log.e("Test", orderId);*/
                order.clientPhone = clientPhone;
                for (int i = list.size() - 1; i >= 0; i -= 1) {
                    if (orderId.equals(list.get(i).name)) {
                        order.startPoint = list.get(i).startPoint;
                        break;
                    }
                }
                /*Toast.makeText(getApplicationContext(), orderId, Toast.LENGTH_LONG).show();*/
                Intent intent = new Intent(OrderActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
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

            if (map != null && (int)map.getKey() == 200) {
                JSONArray arr = (JSONArray)map.getValue();
                for (int i=0; i < arr.length(); ++i) {
                    JSONObject row = arr.getJSONObject(i);
                    if (row.getString("status").equals("new")) {
                        String address = row.getString("address_start");
                        list.add(new Client(row.getString("id"), row.getString("client_phone"), getLatLng(address)));
                    }
                }
            }
        } catch (JSONException e) {
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

            if (map!= null && (int)map.getKey() == HttpStatus.SC_OK)
            {
                InitListView(map);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Сервис недоступен", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mFetchTask = null;
        }
    }

}
