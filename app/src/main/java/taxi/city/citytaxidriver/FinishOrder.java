package taxi.city.citytaxidriver;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class FinishOrder extends ActionBarActivity implements View.OnClickListener {

    TextView tvBeginPoint;
    TextView tvEndPoint;
    TextView tvDistance;
    TextView tvPrice;
    TextView tvTime;
    Button btnFinish;
    private static final int FINISH_ORDER_ID = 2;

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
    }

    protected void GetItems() {
        tvBeginPoint = (TextView) findViewById(R.id.tvBeginPoint);
        tvEndPoint = (TextView) findViewById(R.id.tvEndPoint);
        tvPrice = (TextView) findViewById(R.id.tvPrice);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvTime = (TextView) findViewById(R.id.tvTime);
        btnFinish = (Button) findViewById(R.id.btnDone);
        btnFinish.setOnClickListener(this);
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnDone:
                sendData();
                Intent intent=new Intent();
                intent.putExtra("MESSAGE", "done");
                setResult(FINISH_ORDER_ID,intent);
                finish();//finishing activity
        }
    }

    protected void sendData() {

    }
}
