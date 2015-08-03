package taxi.city.citytaxidriver;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;

import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.core.User;

public class HelpActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        if (User.getInstance() == null || User.getInstance().id == 0) {
            finish();
        }

        App.getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory("ui_views")
                .setLabel("help_view")
                .build());

        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowTitleEnabled(true);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            TextView info_version = (TextView) findViewById(R.id.info_version);
            String info_version_text = getString(R.string.info_version, pInfo.versionName);
            info_version.setText(info_version_text);
        }catch(Exception e){
            //silent
        }
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
