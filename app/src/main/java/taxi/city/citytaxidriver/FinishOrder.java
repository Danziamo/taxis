package taxi.city.citytaxidriver;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import taxi.city.citytaxidriver.Core.Order;
import taxi.city.citytaxidriver.Enums.OStatus;
import taxi.city.citytaxidriver.Service.ApiService;
import taxi.city.citytaxidriver.Utils.Helper;


public class FinishOrder extends ActionBarActivity implements View.OnClickListener {

    TextView tvBeginPoint;
    TextView tvEndPoint;
    TextView tvDistance;
    TextView tvPrice;
    TextView tvTime;
    TextView tvFeePrice;
    TextView tvFeeTime;
    TextView tvTotalPrice;
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
        SetItems();
    }

    protected void SetItems() {
        tvBeginPoint.setText("Начальная точка: " + order.addressStart);
        tvEndPoint.setText(order.addressEnd);
        tvDistance.setText("Путь: " + Helper.getFormattedDistance(order.distance) + " км");
        tvTime.setText("Время: " + Helper.getTimeFromLong(order.time));
        tvPrice.setText("Цена: " + order.getTravelSum() + " сом");
        tvFeeTime.setText("Время ожидания: " + Helper.getTimeFromLong(order.waitTime));
        tvFeePrice.setText("Штраф: " + order.getWaitSum() + " сом");
        tvTotalPrice.setText("Итого: " + order.getTotalSum() + " сом");
    }

    protected void GetItems() {
        tvBeginPoint = (TextView) findViewById(R.id.tvBeginPoint);
        tvEndPoint = (TextView) findViewById(R.id.tvEndPoint);
        tvPrice = (TextView) findViewById(R.id.tvPrice);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvFeePrice = (TextView) findViewById(R.id.tvFeePrice);
        tvFeeTime = (TextView) findViewById(R.id.tvFeeTime);
        tvTotalPrice = (TextView) findViewById(R.id.tvTotalPrice);
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

            order.status = OStatus.FINISHED;
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
            finishTask = null;
            if ((int) map.getKey() == HttpStatus.SC_OK)
            {
                Intent intent=new Intent();
                intent.putExtra("returnCode", true);
                setResult(FINISH_ORDER_ID, intent);
                order.clear();
                finish();//finishing activity
            } else {
                Intent intent=new Intent();
                intent.putExtra("returnCode", false);
                setResult(FINISH_ORDER_ID, intent);
                finish();//finishing activity
            }
        }

        @Override
        protected void onCancelled() {
            finishTask = null;
        }
    }
}
