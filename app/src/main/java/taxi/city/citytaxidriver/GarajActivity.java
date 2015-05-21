package taxi.city.citytaxidriver;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.service.ApiService;
import taxi.city.citytaxidriver.utils.Helper;


public class GarajActivity extends ActionBarActivity implements ActionBar.TabListener {
    TabsPagerAdapter mPageAdapter;
    ViewPager mViewPager;

    private LogoutTask mLogoutTask = null;
    private SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garaj);

        if (User.getInstance() == null || User.getInstance().id == 0) {
            Helper.getUserPreferences(this);
            Helper.getOrderPreferences(this);
            ApiService.getInstance().setToken(User.getInstance().getToken());
        }

        mPageAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setAdapter(mPageAdapter);

        setUpTabs();

    }

    private void setUpTabs() {
        ActionBar ab = getSupportActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ab.setDisplayShowTitleEnabled(true);

        ab.addTab(ab.newTab().setText("Счет").setIcon(R.drawable.ic_action_account).setTabListener(this));
        ab.addTab(ab.newTab().setText("Кабинет").setIcon(R.drawable.ic_action_personal).setTabListener(this));
        ab.addTab(ab.newTab().setText("Транспорт").setIcon(R.drawable.ic_action_transport).setTabListener(this));
        ab.addTab(ab.newTab().setText("История").setIcon(R.drawable.ic_action_history).setTabListener(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_quit:
                signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        pDialog .setTitleText("Вы хотите выйти?")
                .setConfirmText("Выйти")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        logout();
                        sDialog.dismissWithAnimation();
                    }
                })
                .setCancelText("Отмена")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
        switch (tab.getPosition()) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }


    private void logout() {
        if (mLogoutTask != null) return;


        showProgress(true);
        mLogoutTask = new LogoutTask();
        mLogoutTask.execute((Void) null);
    }

    private class LogoutTask extends AsyncTask<Void, Void, JSONObject> {

        LogoutTask() {}

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                JSONObject onlineStatus = new JSONObject();
                onlineStatus.put("online_status", "offline");
                onlineStatus = ApiService.getInstance().patchRequest(onlineStatus, "users/" + String.valueOf(User.getInstance().id) +"/");
            } catch (JSONException ignored) {}
            return ApiService.getInstance().logoutRequest(null, "logoout/");
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            mLogoutTask = null;
            showProgress(false);
            Intent intent = new Intent(GarajActivity.this, LoginActivity.class);
            intent.putExtra("finish", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
            startActivity(intent);
            finish();
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
            mLogoutTask = null;
        }
    }

    public void showProgress(final boolean show) {
        if (show) {
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper()
                    .setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Выход");
            pDialog.setCancelable(true);
            pDialog.show();
        } else {
            pDialog.dismissWithAnimation();
        }
    }
}
