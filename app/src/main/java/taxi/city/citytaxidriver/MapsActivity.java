package taxi.city.citytaxidriver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import taxi.city.citytaxidriver.Core.Client;
import taxi.city.citytaxidriver.Core.GlobalParameters;
import taxi.city.citytaxidriver.Core.Order;
import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Enums.OStatus;
import taxi.city.citytaxidriver.Service.ApiService;
import taxi.city.citytaxidriver.Utils.Helper;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static final String PREFS_NAME = "OrderPrefsFile";
    private SharedPreferences settings;

    private static final int FINISH_ORDER_ID = 2;
    private static final int MAKE_ORDER_ID = 1;
    private static final int ORDER_DETAILS_ID = 3;
    private static final String TAG = "MapsActivity";

    private SendPostRequestTask sendTask;

    LinearLayout llMain;
    TextView tvDistance;
    TextView tvPrice;
    //TextView tvSpeed;
    //TextView tvTime;
    TextView tvFeeTime;
    TextView tvFeePrice;
    //TextView tvTotalSum;

    Order order = Order.getInstance();
    ApiService api = ApiService.getInstance();
    GlobalParameters gp = GlobalParameters.getInstance();
    User user;

    Location prev;
    double distance;
    double price;
    double ratio = 12;
    double freeMeters = 2000;
    double startPrice = 50;
    double waitSum = 0;
    long time;

    Button btnOkAction;
    Button btnSettingsCancel;
    Button btnInfo;
    Button btnWait;
    LinearLayout llButtonTop;
    LinearLayout llButtonBottom;

    Location location;
    List<Polyline> polylines = new ArrayList<>();

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    DecimalFormat df = new DecimalFormat("#.##");

    long startTime = 0;
    long pauseTotalTime = 0;
    long pauseSessionTime = 0;
    long pauseStartTime = 0;

    Handler globalTimerHandler = new Handler();
    Runnable globalTimerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis();
            double seconds = (double) (millis / 1000);

            if (Helper.isOrderActive(order)) {
                saveToPreferences();
                if (seconds % 30 < 1) SendPostRequest(order.status, order.id);

            }
            globalTimerHandler.postDelayed(this, 1000);
        }
    };

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            double seconds = (double) (millis / 1000);
            time = (long)seconds;
            order.time = time;
            updateLabels();
            timerHandler.postDelayed(this, 1000);
        }
    };

    Handler pauseHandler = new Handler();
    Runnable pauseRunnable = new Runnable() {
        @Override
        public void run() {
        long millis = System.currentTimeMillis() - pauseStartTime;

        pauseSessionTime = (long) (millis/ 1000);
        long tempTime = pauseTotalTime + pauseSessionTime;
        if (tempTime > 5 * 60) {
            if (tempTime <= 5*60) {
                waitSum = 0;
            } else if (tempTime <= 15*60) {
                waitSum = Math.round((double)3*(tempTime-5*60)/60);
            } else {
                waitSum = Math.round(3*10 + (double)(tempTime - 15*60)/60);
            }
            //waitSum = tempTime <= 15*60 ? (double)(3*tempTime/60) : (double)(15*tempTime/60);
        }
        order.waitTime = pauseTotalTime + pauseSessionTime;
        order.waitSum = waitSum;
        tvFeePrice.setText(df.format(order.getWaitSum()));
        tvFeeTime.setText(Helper.getTimeFromLong(pauseTotalTime + pauseSessionTime));

        pauseHandler.postDelayed(this, 1000);
        }
    };

    private void updateLabels() {
        //tvTime.setText("Время: " + Helper.getTimeFromLong(order.time));
        //tvTotalSum.setText("Общая сумма: " + df.format(order.getTotalSum()) + " сом");
        tvDistance.setText(df.format(distance / 1000));
        tvPrice.setText(df.format(order.getTravelSum()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        user = User.getInstance();

        checkUserSession();

        setUpMapIfNeeded();

        CheckEnableGPS();

        SetGooglePlayServices();

        Initialize();
        order = Order.getInstance();
        getPreferences();

        globalTimerHandler.postDelayed(globalTimerRunnable, 0);
        updateViews();

        if (order.id == 0) {
            SetDefaultValues();
        }

        SetLocationRequest();
    }

    private void checkUserSession() {
        if (user == null || user.id == 0)
        {
            Toast.makeText(getApplicationContext(), "Сессия вышла, пожалуйста перезайдите", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void saveToPreferences() {
        settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("orderDistance", (float)order.distance);
        editor.putInt("orderId", order.id);
        editor.putFloat("orderFixedPrice", (float)order.fixedPrice);
        editor.putLong("orderTime", order.time);
        editor.putLong("orderWaitTime", order.waitTime);
        editor.putString("orderPhone", order.clientPhone);
        editor.putString("orderStartPoint", Helper.getFormattedLatLng(order.startPoint));
        editor.putString("orderStatus", order.status == null ? null : order.status.toString());
        editor.apply();
    }

    private void clearPreferences() {
        settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear().apply();
    }

    private void resetPreferences() {
        settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("orderId", 0);
        editor.putString("status", null);
        editor.apply();
    }

    private boolean ifPreferenceActive() {
        settings = getSharedPreferences(PREFS_NAME, 0);
        return settings.contains("orderId") && settings.contains("orderStatus") && !(settings.getInt("orderId", 0) == 0 || settings.getString("orderStatus", null) == null);
    }

    private void getPreferences() {
        settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.contains("orderId")) {
            String pStatus = settings.getString("orderStatus", "");
            if (pStatus.equals(OStatus.ONTHEWAY.toString()))
                order.status = OStatus.PENDING;
            else
                order.status = Helper.getStatus(pStatus);

            order.id = settings.getInt("orderId", 0);
            order.clientPhone = settings.getString("orderPhone", null);
            order.time = settings.getLong("orderTime", 0);
            order.fixedPrice = (double)settings.getFloat("orderFixedPrice", 0);
            order.distance = (double)settings.getFloat("orderDistance", 0);
            order.waitTime = settings.getLong("orderWaitTime", 0);
            order.startPoint = Helper.getLatLng(settings.getString("orderStartPoint", null));
            if (mMap != null && order.startPoint != null && order.clientPhone != null) {
                setClientLocation();
            }
            distance = 1000*order.distance;
            startTime = System.currentTimeMillis() - order.time*1000;
            pauseTotalTime = order.waitTime;
            if (distance > freeMeters)
                price = Math.round(startPrice +  ratio*(distance-freeMeters)/1000);
            else
                price = 60;
            order.sum = price;
            if (order.waitTime > 5 * 60) {
                if (order.waitTime <= 5*60) {
                    order.waitSum = 0;
                } else if (order.waitTime <= 15*60) {
                    order.waitSum = Math.round((double)3*(order.waitTime-5*60)/60);
                } else {
                    order.waitSum = Math.round(3*10 + (double)(order.waitTime - 15*60)/60);
                }
            }
            pauseStartTime = System.currentTimeMillis() - order.waitTime;
            pauseSessionTime = 0;

            timerHandler.postDelayed(timerRunnable, 0);
            //tvSpeed.setText("Скорость: 0 км/ч");
            //tvTime.setText("Время: " + Helper.getTimeFromLong(order.time));
            tvPrice.setText(df.format(order.getTravelSum()));
            tvDistance.setText(df.format(distance / 1000));
            tvFeePrice.setText(df.format(order.getWaitSum()));
            tvFeeTime.setText(Helper.getTimeFromLong(pauseTotalTime + pauseSessionTime));
            //tvTotalSum.setText("Общая сумма: " + df.format(order.getTotalSum()) + " сом");
        }
    }

    private void CheckEnableGPS(){
        String provider = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!provider.equals("")){
            //GPS Enabled
            Toast.makeText(this, "GPS Enabled: " + provider,
                    Toast.LENGTH_LONG).show();
        }else{
            displayPromptForEnablingGPS();
        }
    }

    public void displayPromptForEnablingGPS()
    {

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(MapsActivity.this);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Активируйте геолокацию.";

        builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                MapsActivity.this.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    private void SetGooglePlayServices() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void SetLocationRequest() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(10)
                .setInterval(1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds
    }

    private void Initialize() {
        prev = null;
        tvDistance = (TextView) findViewById(R.id.textViewDistance);
        tvPrice = (TextView) findViewById(R.id.textViewSum);
        llMain = (LinearLayout) findViewById(R.id.mainLayout);
        //tvSpeed = (TextView) findViewById(R.id.textViewSpeed);
        //tvTime = (TextView) findViewById(R.id.textViewTime);
        tvFeePrice = (TextView) findViewById(R.id.textViewWaitSum);
        tvFeeTime = (TextView) findViewById(R.id.textViewWaitTime);
        //tvTotalSum = (TextView) findViewById(R.id.textViewMapsTotalSum);

        btnInfo = (Button) findViewById(R.id.buttonAdditionalInfo);
        btnOkAction = (Button) findViewById(R.id.buttonStartAction);
        btnSettingsCancel = (Button)findViewById(R.id.buttonDeclineSettings);
        btnWait = (Button)findViewById(R.id.buttonWaitTrip);

        llButtonTop = (LinearLayout) findViewById(R.id.linearLayoutWaitInfo);
        llButtonBottom = (LinearLayout) findViewById(R.id.linearLayoutStartCancelMap);

        btnInfo.setOnClickListener(this);
        btnOkAction.setOnClickListener(this);
        btnSettingsCancel.setOnClickListener(this);
        btnWait.setOnClickListener(this);
    }

    private void updateViews() {
        if (order.id == 0 || order.status == OStatus.NEW) {
            order.clear();
            mMap.clear();
            llButtonTop.setVisibility(View.GONE);
            btnOkAction.setText("Заказы");
            btnOkAction.setBackgroundResource(R.drawable.button_shape_black);
            btnOkAction.setTextColor(Color.WHITE);
            btnSettingsCancel.setText("Настройки");
            btnSettingsCancel.setBackgroundResource(R.drawable.button_shape_black);
            btnSettingsCancel.setTextColor(Color.WHITE);
            llMain.setVisibility(View.GONE);
        } else {
            if (order.status == OStatus.ACCEPTED) {
                btnOkAction.setBackgroundResource(R.drawable.button_shape_green);
                btnOkAction.setTextColor(Color.BLACK);
                btnOkAction.setText("На месте");
                btnInfo.setText("Доп. инфо");
                btnSettingsCancel.setText("Отказ");
                btnSettingsCancel.setBackgroundResource(R.drawable.button_shape_red);
                btnWait.setVisibility(View.INVISIBLE);
                llButtonTop.setVisibility(View.VISIBLE);
            } else if (order.status == OStatus.WAITING) {
                btnOkAction.setBackgroundResource(R.drawable.button_shape_green);
                btnOkAction.setTextColor(Color.BLACK);
                btnOkAction.setText("На борту");
                btnInfo.setText("Доп. инфо");
                btnSettingsCancel.setText("Отказ");
                btnSettingsCancel.setBackgroundResource(R.drawable.button_shape_red);
                btnWait.setVisibility(View.INVISIBLE);
            } else if (order.status == OStatus.PENDING) {
                btnOkAction.setBackgroundResource(R.drawable.button_shape_green);
                btnOkAction.setTextColor(Color.BLACK);
                btnOkAction.setText("На месте");
                btnInfo.setText("Доп. инфо");
                btnWait.setText("Продолжить");
                btnSettingsCancel.setText("Настройки");
                btnSettingsCancel.setBackgroundResource(R.drawable.button_shape_black);
            } else if (order.status == OStatus.ONTHEWAY) {
                btnWait.setVisibility(View.VISIBLE);
                btnWait.setText("Ожидание");
                btnOkAction.setBackgroundResource(R.drawable.button_shape_green);
                btnOkAction.setTextColor(Color.BLACK);
                btnOkAction.setText("На месте");
                btnSettingsCancel.setText("Настройки");
                btnSettingsCancel.setBackgroundResource(R.drawable.button_shape_black);
            } else {
                mMap.clear();
                btnOkAction.setText("Заказы");
                btnOkAction.setBackgroundResource(R.drawable.button_shape_black);
                btnOkAction.setTextColor(Color.WHITE);
                btnSettingsCancel.setText("Настройки");
                btnSettingsCancel.setBackgroundResource(R.drawable.button_shape_black);
                btnSettingsCancel.setTextColor(Color.WHITE);
                llButtonTop.setVisibility(View.GONE);
            }
        }
    }

    private void SetDefaultValues() {
        pauseTotalTime = 0;
        pauseSessionTime = 0;
        distance = 0;
        price = startPrice;
        order.sum = startPrice;
        order.waitTime = 0;
        resetTimer();

        //tvSpeed.setText("Скорость: 0 км/ч");
        tvDistance.setText(df.format(distance / 1000));
        tvPrice.setText(df.format(price));
        //tvTime.setText("Время: " + "00:00:00");
        tvFeePrice.setText(null);
        tvFeeTime.setText(null);
        //tvTotalSum.setText("Общая цена: " + df.format(price + waitSum) + " сом");
    }

    private void resetTimer() {
        order.time = 0;
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        startLocationUpdates();

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        this.location = location;
        if (location != null) {
            order.endPoint = new LatLng(location.getLatitude(), location.getLongitude());
            gp.currPosition = new LatLng(location.getLatitude(), location.getLongitude());
        }

        if (location != null) {
            gp.currPosition = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
            handleNewLocation(location);
        }
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void handleNewLocation(Location location) {
        location.setAltitude(0);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        double speed = location.getSpeed();
        boolean ifSession = (order.id != 0 && order.status == OStatus.ONTHEWAY);
        int zoom = ifSession ? 17 : 15;
        int bearing = ifSession ? (int)location.getBearing() : 0;
        int tilt = ifSession ? 45 : 0;

        if (ifSession && prev != null){
            distance += prev.distanceTo(location);
            if (distance > freeMeters)
                price = Math.round(startPrice +  ratio*(distance-freeMeters)/1000);

            order.distance = distance/1000;
            order.sum = price;
            order.endPoint = latLng;
            //tvSpeed.setText("Скорость: " + df.format(speed * 3.6) + " км/ч");

            if (prev != null) {
                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(prev.getLatitude(), prev.getLongitude()), latLng)
                        .width(12)
                        .color(0x7F0000FF)
                        .geodesic(true));

                polylines.add(line);
            }
        }

        order.endPoint = latLng;
        if (prev == null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
        prev = location;

        if (ifSession) {
            CameraPosition cp = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(zoom)
                    .bearing(bearing)
                    .tilt(tilt)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        //
        // More about this in the next section.
        if (result.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                result.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + result.getErrorCode());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (order == null || order.id == 0) {
            resetPreferences();
            clearPreferences();
        }
        updateViews();
        setUpMapIfNeeded();
        if (mMap != null && order != null && order.startPoint != null && order.clientPhone != null) {
            if (order.status != OStatus.ONTHEWAY && order.status != OStatus.PENDING) setClientLocation();
        }
        checkUserSession();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }*/
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        handleNewLocation(this.location);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            if (mMap != null) {
                if (order.startPoint != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(order.startPoint, 15));
                }
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.buttonStartAction:
                if (order == null || order.id == 0 || order.status == OStatus.FINISHED) {
                    OpenOrder();
                } else if (order.status == OStatus.ACCEPTED) {
                    order.status = OStatus.WAITING;
                    SendPostRequest(OStatus.WAITING, order.id);
                } else if (order.status == OStatus.WAITING) {
                    SetDefaultValues();
                    llMain.setVisibility(View.VISIBLE);
                    order.status = OStatus.ONTHEWAY;
                    SendPostRequest(OStatus.ONTHEWAY, order.id);
                } else if (order.status == OStatus.ONTHEWAY || order.status == OStatus.PENDING) {
                    EndTrip();
                }
                break;
            case R.id.buttonWaitTrip:
                if (order.status == OStatus.ONTHEWAY) {
                    order.status = OStatus.PENDING;
                    SendPostRequest(OStatus.PENDING, order.id);
                    pauseSessionTime = 0;
                    pauseStartTime = System.currentTimeMillis();
                    pauseHandler.postDelayed(pauseRunnable, 0);
                } else if (order.status == OStatus.PENDING) {
                    order.status = OStatus.ONTHEWAY;
                    SendPostRequest(OStatus.ONTHEWAY, order.id);
                    pauseHandler.removeCallbacks(pauseRunnable);
                    pauseTotalTime += pauseSessionTime;
                }
                updateViews();
                break;
            case R.id.buttonDeclineSettings:
                if (order.status == OStatus.WAITING || order.status == OStatus.ACCEPTED) {
                    order.status = OStatus.NEW;
                    timerHandler.removeCallbacks(timerRunnable);
                    pauseHandler.removeCallbacks(pauseRunnable);
                    SendPostRequest(order.status, order.id);
                } else {
                    goToSettings();
                }
                break;
            case R.id.buttonAdditionalInfo:
                if (order.status == OStatus.ACCEPTED || order.status == OStatus.WAITING) {
                    goToOrderDetails();
                } else {
                    goToFinishOrderDetails();
                }
                break;
        }
        updateViews();
    }

    private void goToSettings() {
        checkPreviousOrder();
        Intent intent = new Intent(this, GarajActivity.class);
        startActivity(intent);
    }

    private void goToOrderDetails() {
        Intent intent = new Intent(this, OrderDetailsActivity.class);
        intent.putExtra("DATA", new Client(order));
        startActivityForResult(intent, ORDER_DETAILS_ID);
    }

    private void goToFinishOrderDetails() {
        Intent intent = new Intent(this, FinishOrderDetailsActivity.class);
        intent.putExtra("DATA", new Client(order));
        startActivity(intent);
    }

    private void EndTrip() {
        Intent intent = new Intent(this, FinishOrderDetailsActivity.class);
        intent.putExtra("DATA", new Client(order));
        startActivityForResult(intent, FINISH_ORDER_ID);
    }

    private void setClientLocation() {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(order.startPoint).title(order.clientPhone));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(order.startPoint, 15));
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                final AlertDialog.Builder builder =
                        new AlertDialog.Builder(MapsActivity.this, R.style.DialogBoxTheme);
                //final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
                final String message = "Вы уверены что хотите позвонить?";
                final String title = order.clientPhone;

                builder.setMessage(message)
                        .setTitle(title)
                        .setPositiveButton("Позвонить",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d, int id) {
                                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                                        callIntent.setData(Uri.parse("tel:"+order.clientPhone));
                                        startActivity(callIntent);
                                        d.dismiss();
                                    }
                                })
                        .setNegativeButton("Отмена",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d, int id) {
                                        d.cancel();
                                    }
                                });
                builder.create().show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FINISH_ORDER_ID) {
            if (data != null) {
                if (data.getBooleanExtra("returnCode", false)) {
                    Toast.makeText(getApplicationContext(), "Заказ завершен", Toast.LENGTH_SHORT).show();
                    resetPreferences();
                    clearPreferences();
                } else {
                    saveToPreferences();
                    order.clear();
                    Toast.makeText(getApplicationContext(), "Заказ завершен. Ошибка при отправке данных на сервер", Toast.LENGTH_SHORT).show();
                }
                ClearMapFromLines();
                timerHandler.removeCallbacks(timerRunnable);
                pauseHandler.removeCallbacks(pauseRunnable);
                mMap.clear();
            }
            updateViews();
        }
        if (requestCode == MAKE_ORDER_ID) {
            if (data != null && data.getBooleanExtra("returnCode", false)) {
                timerHandler.postDelayed(timerRunnable, 0);
                order.endPoint = new LatLng(location.getLatitude(), location.getLongitude());
                setClientLocation();
            } else if (data != null) {

            }
            checkUserSession();
            updateViews();
        }
        if (requestCode == ORDER_DETAILS_ID) {
            if (order.status == OStatus.ONTHEWAY) {
                setClientLocation();
                timerHandler.postDelayed(timerRunnable, 0);
            }
        }
    }

    private void ClearMapFromLines() {
        for(Polyline line : polylines)
        {
            line.remove();
        }
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private void checkPreviousOrder() {
        if (order.id == 0 || order.status == OStatus.FINISHED) {
            if (ifPreferenceActive()) {
                getPreferences();
                SendPostRequest(order.status, order.id);
            }
        }
    }

    private void OpenOrder() {
        Intent intent = new Intent(this, OrderActivity.class);
        intent.putExtra("NEW", true);
        startActivityForResult(intent, MAKE_ORDER_ID);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void SendPostRequest(OStatus status, int orderId) {
        if (sendTask != null || status == null || orderId == 0) {
            return;
        }

        if (location != null) {
            order.endPoint = new LatLng(location.getLatitude(), location.getLongitude());
        }

        if (order.status == OStatus.ACCEPTED || order.status == OStatus.WAITING) {
            order.time = 0;
        }

        sendTask = new SendPostRequestTask(status, orderId);
        sendTask.execute((Void) null);
    }

    private class SendPostRequestTask extends AsyncTask<Void, Void, JSONObject> {
        private OStatus status;
        private int driver;
        private int mOrderId = 0;

        SendPostRequestTask(OStatus type, int orderId) {
            status = type;
            driver = user.id;
            mOrderId = orderId;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            JSONObject data = new JSONObject();
            JSONObject result = null;
            try {
                if (status == OStatus.NEW) {
                    data.put("status", status);
                    data.put("driver", JSONObject.NULL);
                } else {
                    String travelTime = Helper.getTimeFromLong(order.time, order.status);
                    data.put("status", status);
                    data.put("driver", status == OStatus.NEW ? JSONObject.NULL : driver);
                    data.put("order_sum", order.getTotalSum());
                    data.put("wait_time_price", order.getWaitSum());
                    data.put("address_stop", status == OStatus.NEW ? JSONObject.NULL : Helper.getFormattedLatLng(order.endPoint));
                    data.put("wait_time", Helper.getTimeFromLong(order.waitTime));
                    data.put("order_distance", (double) Math.round(order.distance * 100) / 100);
                    data.put("order_travel_time", travelTime);
                }
                JSONObject checkObject = api.getOrderRequest(null, "orders/" + mOrderId + "/");
                if (checkObject != null && checkObject.getInt("status_code") == HttpStatus.SC_OK) {
                    String checkStatus = checkObject.getString("status");
                    if (checkStatus == null || checkStatus.equals(OStatus.CANCELED.toString())) {
                        checkObject.put("status_code", 999);
                        result = checkObject;
                    } else {
                        result = api.patchRequest(data, "orders/" + mOrderId + "/");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                result = null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            sendTask = null;
            try {
                if (result != null && result.getInt("status_code") == HttpStatus.SC_OK) {
                    Toast.makeText(getApplicationContext(), "Заказ обновлён", Toast.LENGTH_SHORT).show();
                    if (status == OStatus.FINISHED || status == OStatus.NEW) {
                        order.clear();
                        resetPreferences();
                        clearPreferences();
                    }
                } else if (result != null && result.getInt("status_code") == 999) {
                    Toast.makeText(getApplicationContext(), "Клиент отменил заказ: " + result.getString("description"), Toast.LENGTH_LONG).show();
                    order.clear();
                    resetPreferences();
                    clearPreferences();
                    updateViews();
                } else {
                    Toast.makeText(getApplicationContext(), "Не удалось отправить данные на сервер", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Внутрення ошибка", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            sendTask = null;
        }
    }
}