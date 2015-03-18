package taxi.city.citytaxidriver;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import taxi.city.citytaxidriver.Core.Client;
import taxi.city.citytaxidriver.Core.ClientAdapter;


public class OrderActivity extends ActionBarActivity {

    private ArrayList<Client> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        InitListView();

        ListView lvMain = (ListView) findViewById(R.id.orderList);
        ClientAdapter adapter = new ClientAdapter(this, list);
        lvMain.setAdapter(adapter);
    }

    private void InitListView() {
        list.clear();
        list.add(new Client("Kesh", "0555983099"));
        list.add(new Client("Dan", "0555992938"));
        list.add(new Client("Katya", "0556437951"));
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

}
