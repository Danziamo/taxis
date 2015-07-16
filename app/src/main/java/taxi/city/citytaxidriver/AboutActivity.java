package taxi.city.citytaxidriver;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;

import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.core.User;

public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if (User.getInstance() == null || User.getInstance().id == 0) finish();

        App.getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory("ui_views")
                .setLabel("about_view")
                .build());
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
