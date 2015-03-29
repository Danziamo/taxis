package taxi.city.citytaxidriver;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import taxi.city.citytaxidriver.Core.Order;
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
                sendData();
                Intent intent=new Intent();
                intent.putExtra("MESSAGE", "done");
                setResult(FINISH_ORDER_ID, intent);
                order.clear();
                finish();//finishing activity
        }
    }

    private void sendData() {
        try {
            JSONObject json = new JSONObject();
            json.put("client_phone", "tamasha");
            //json.put("password", mPassword);
            api.getDataFromPostRequest(json, "/orders");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
