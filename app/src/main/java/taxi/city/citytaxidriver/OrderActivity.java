package taxi.city.citytaxidriver;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import taxi.city.citytaxidriver.core.Client;
import taxi.city.citytaxidriver.core.ClientAdapter;
import taxi.city.citytaxidriver.core.Order;
import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.enums.OStatus;
import taxi.city.citytaxidriver.service.ApiService;

public class OrderActivity extends ActionBarActivity implements View.OnClickListener {

    private ArrayList<Client> list = new ArrayList<>();
    private Order order = Order.getInstance();
    private ApiService api = ApiService.getInstance();
    private User user;
    private FetchOrderTask mFetchTask = null;
    private SweetAlertDialog pDialog;

    private Client client;
    ListView lvMain;
    Button btnMap;
    Button btnRefresh;
    Button btnMoreOrders;
    private boolean isNew;
    private int limit = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        user = User.getInstance();
        limit = 10;

        Intent intent = getIntent();
        isNew = intent.getExtras().getBoolean("NEW", false);

        lvMain = (ListView) findViewById(R.id.orderList);
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    String text = ((TextView) view.findViewById(R.id.orderId)).getText().toString();
                    int orderId = Integer.valueOf(text);

                    for (int i = list.size() - 1; i >= 0; i -= 1) {
                        if (orderId == list.get(i).id) {
                            client = list.get(i);
                            break;
                        }
                    }
                    goOrderDetails(client);
                }
                catch (Exception e) {
                }
            }
        });

        btnMap = (Button)findViewById(R.id.buttonMap);
        btnRefresh = (Button)findViewById(R.id.buttonRefresh);
        btnMoreOrders = (Button)findViewById(R.id.buttonMoreOrder);

        btnMap.setOnClickListener(this);
        btnRefresh.setOnClickListener(this);
        btnMoreOrders.setOnClickListener(this);

        TextView tvTitle = (TextView) findViewById(R.id.textViewOrderTitle);

        if (!isNew) {
            tvTitle.setText("История заказов:");
            btnMap.setText("Назад");
            fetchData();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (isNew) {
            list.clear();
            lvMain.setAdapter(null);
            if (user != null && user.id != 0) fetchData();
        }
    }

    private void InitListView(JSONArray array) {
        list.clear();
        try {
            for (int i=0; i < array.length(); ++i) {
                JSONObject row = array.getJSONObject(i);
                if (!row.has("status") || row.getString("status").equals(OStatus.CANCELED.toString()))
                    continue;
                Client client = new Client(row, user.id, true);

                list.add(client);

            }
            ClientAdapter adapter = new ClientAdapter(OrderActivity.this, list);
            lvMain.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void goOrderDetails(Client mClient) {
        if (isNew) {
            Intent intent = new Intent(this, OrderDetailsActivity.class);
            intent.putExtra("DATA", mClient);
            intent.putExtra("ACTIVE", false);
            startActivityForResult(intent, 1);
        } else {
            Intent intent = new Intent(this, FinishOrderDetailsActivity.class);
            intent.putExtra("DATA", mClient);
            startActivityForResult(intent, 2);
        }
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
            }
        }
    }

    private void fetchData() {
        if (mFetchTask != null) {
            return;
        }

        showProgress(true);
        mFetchTask = new FetchOrderTask();
        mFetchTask.execute((Void) null);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonMap:
                finish();
                break;
            case R.id.buttonMoreOrder:
                limit += 10;
                fetchData();
                break;
            default:
                fetchData();
        }
    }

    public class FetchOrderTask extends AsyncTask<Void, Void, JSONArray>{

        FetchOrderTask() {}

        @Override
        protected JSONArray doInBackground(Void... params) {

            JSONArray array = null;
            try {
                array = new JSONArray();
                JSONObject result = new JSONObject();
                if (isNew) {
                    if (order.id == 0 || order.status == OStatus.FINISHED || order.status == null) {
                        result = api.getDataFromGetRequest(null, "orders/?status=new");
                        if (result.getInt("status_code") == HttpStatus.SC_OK) {
                            JSONArray tempArray = result.getJSONArray("result");
                            for (int i = 0; i < tempArray.length(); ++i) {
                                array.put(tempArray.getJSONObject(i));
                            }
                        }
                    }
                    if (order.id != 0) array.put(order.getOrderAsJson());
                } else {
                    result = api.getDataFromGetRequest(null, "orders/?driver=" + user.id + "&status=finished&ordering=-id&limit=" + limit);
                    if (result.getInt("status_code") == HttpStatus.SC_OK) {
                        JSONArray tempArray = result.getJSONArray("result");
                        for (int i = 0; i < tempArray.length() && i < 10; ++i) {
                            array.put(tempArray.getJSONObject(i));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                array = null;
            }

            return array;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            mFetchTask = null;
            showProgress(false);
            if (result != null) {
                InitListView(result);
            } else {
                Toast.makeText(OrderActivity.this, "Не удалось получить данные с сервера", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mFetchTask = null;
        }
    }

    public void showProgress(final boolean show) {
        if (show) {
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper()
                    .setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Обновление");
            pDialog.setCancelable(true);
            pDialog.show();
        } else {
            if (pDialog != null) pDialog.dismissWithAnimation();
        }
    }
}
