package taxi.city.citytaxidriver;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import taxi.city.citytaxidriver.Core.Order;
import taxi.city.citytaxidriver.Enums.OrderStatus;
import taxi.city.citytaxidriver.Service.ApiService;


public class FinishOrder extends ActionBarActivity implements View.OnClickListener {

    TextView tvBeginPoint;
    TextView tvEndPoint;
    TextView tvDistance;
    TextView tvPrice;
    TextView tvTime;
    TextView tvFeePrice;
    TextView tvFeeTime;
    Button btnFinish;
    private static final int FINISH_ORDER_ID = 2;
    private ApiService api = ApiService.getInstance();
    private Order order = Order.getInstance();
    private FinishOrderTask finishTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_order);
        GetItems();
        SetItems(getIntent().getExtras());
    }

    protected void SetItems(Bundle b) {
        tvBeginPoint.setText("Начальная точка: " + b.getString("BeginPoint"));
        tvEndPoint.setText("Конечная точка: " + b.getString("EndPoint"));
        tvDistance.setText("Путь: " + b.getString("Distance") + " км");
        tvTime.setText("Время: " + b.getString("Time") + " мин");
        tvPrice.setText("Цена: " + b.getString("Price") + " сом");
        tvFeeTime.setText("Время ожидания: " + b.getString("FeeTime") + " мин");
        tvFeePrice.setText("Штраф: " + b.getString("FeePrice") + " с");
        order.distance = b.getDouble("Distance");
        order.sum = b.getDouble("Price");
        order.time = b.getLong("Time");
    }

    protected void GetItems() {
        tvBeginPoint = (TextView) findViewById(R.id.tvBeginPoint);
        tvEndPoint = (TextView) findViewById(R.id.tvEndPoint);
        tvPrice = (TextView) findViewById(R.id.tvPrice);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvFeePrice = (TextView) findViewById(R.id.tvFeePrice);
        tvFeeTime = (TextView) findViewById(R.id.tvFeeTime);
        btnFinish = (Button) findViewById(R.id.btnDone);
        btnFinish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnDone:
                sendPostRequest();
        }
    }

    private void sendPostRequest() {
        if (finishTask != null) {
            return;
        }

        finishTask = new FinishOrderTask();
        finishTask.execute((Void) null);
    }

    public class FinishOrderTask extends AsyncTask<Void, Void, Map.Entry> {

        FinishOrderTask() {}

        @Override
        protected Map.Entry doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.

            order.status = OrderStatus.STATUS.FINISHED;
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
                finishTask = null;
                Intent intent=new Intent();
                intent.putExtra("MESSAGE", "Заказ окончен");
                setResult(FINISH_ORDER_ID, intent);
                order.clear();
                finish();//finishing activity
            } else {
                Intent intent=new Intent();
                intent.putExtra("MESSAGE", "Ошибка при отправке на сервер");
                setResult(FINISH_ORDER_ID, intent);
                order.clear();
                finish();//finishing activity
            }
        }

        @Override
        protected void onCancelled() {
            finishTask = null;
        }
    }
}
