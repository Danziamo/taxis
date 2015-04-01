package taxi.city.citytaxidriver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import taxi.city.citytaxidriver.Core.Order;
import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Service.ApiService;

public class MapsActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private Order order;
    private static final int FINISH_ORDER_ID = 2;
    private static final int MAKE_ORDER_ID = 1;
    private static final String TAG = "MapsActivity";

    LinearLayout llMain;
    TextView tvDistance;
    TextView tvPrice;
    TextView tvSpeed;
    TextView tvTime;
    TextView tvPause;
    TextView tvFeeTime;
    TextView tvFeePrice;

    Location prev;
    double distance;
    double price;
    double ratio = 10;
    double freeMeters = 2000;
    double startPrice = 60;
    double feePrice = 0;
    double time;

    Button btnBeginTrip;
    Button btnPauseTrip;
    Button btnEndTrip;
    Location location;
    List<Polyline> polylines = new ArrayList<>();

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    DecimalFormat df = new DecimalFormat("#.##");

    boolean isPause = false;
    long startTime = 0;
    double pauseTotalTime = 0;
    double pauseSessionTime = 0;
    long pauseStartTime = 0;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            if (!isPause) {
                long millis = System.currentTimeMillis() - startTime;
                double seconds = (double) (millis / 1000);

                time = (seconds - pauseTotalTime)/ 60;
                tvTime.setText("Время: " + df.format(time) + " мин");
            } else {
                long millis = System.currentTimeMillis() - pauseStartTime;

                pauseSessionTime = (double) (millis/ 1000);
                //tvPause.setText("Время ожидания: " + df.format(pauseSessionTime / 60) + " мин");
                tvFeePrice.setText("Штраф: " + df.format(feePrice) + " с");
                tvFeeTime.setText("Время ожидания: " + df.format(pauseTotalTime/60 + pauseSessionTime/60) + " мин");
            }
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        CheckEnableGPS();

        SetGooglePlayServices();

        Initialize();
        SetDefaultValues();
        btnPauseTrip.setEnabled(false);

        SetLocationRequest();
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
        distance = 0;
        price = startPrice;
        prev = null;
        tvDistance = (TextView) findViewById(R.id.textViewDistance);
        tvPrice = (TextView) findViewById(R.id.textViewPrice);
        btnBeginTrip = (Button) findViewById(R.id.beginTrip);
        btnEndTrip = (Button) findViewById(R.id.endTrip);
        btnPauseTrip = (Button) findViewById(R.id.pauseTrip);
        llMain = (LinearLayout) findViewById(R.id.mainLayout);
        tvSpeed = (TextView) findViewById(R.id.textViewSpeed);
        tvTime = (TextView) findViewById(R.id.textViewTime);
        tvPause = (TextView) findViewById(R.id.pauseText);
        tvFeePrice = (TextView) findViewById(R.id.textViewFeePrice);
        tvFeeTime = (TextView) findViewById(R.id.textViewFeeTime);

        btnBeginTrip.setEnabled(false);
        btnPauseTrip.setEnabled(false);
        btnEndTrip.setEnabled(false);
        btnBeginTrip.setOnClickListener(this);
        btnEndTrip.setOnClickListener(this);
        btnPauseTrip.setOnClickListener(this);

    }

    private void SetDefaultValues() {
        startTime = System.currentTimeMillis();
        distance = 0;
        price = startPrice;

        tvSpeed.setText("Скорость: 0 км/ч");
        tvDistance.setText("Расстояние: " + df.format(distance / 1000) + " км");
        tvPrice.setText("Цена: " + df.format(price) + " сом");
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
        /*if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }*/
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Connected to Google Play services!
        // The good stuff goes here.
        Log.i(TAG, "Location services connected.");
        startLocationUpdates();

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
            handleNewLocation(location);
        }
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        location.setAltitude(0);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        double speed = location.getSpeed();
        boolean ifSession = order.id != 0;
        int zoom = ifSession ? 17 : 15;
        int bearing = ifSession ? (int)location.getBearing() : 0;
        int tilt = ifSession ? 45 : 0;

        if (ifSession && !isPause){
            distance += prev.distanceTo(location);
            if (pauseTotalTime > 5 * 60) {
                feePrice = pauseTotalTime <= 15*60 ? 3*pauseTotalTime/60 : 15*pauseTotalTime/60;
                if (pauseTotalTime > 15*60) {
                    feePrice += 5 * pauseTotalTime/60;
                }
            }
            if (distance >= freeMeters)
                price = startPrice +  ratio*distance/1000;
            tvDistance.setText("Расстояние: " + df.format(distance / 1000) + "км");
            tvPrice.setText("Цена: " + df.format(price) + "сом");
            tvSpeed.setText("Скорость: " + df.format(speed * 3.6) + " км/ч");

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

        if (ifSession && !isPause) {
            CameraPosition cp = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(zoom)
                    .bearing(bearing)
                    .tilt(tilt)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
        }
        /*else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }*/
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
        setUpMapIfNeeded();
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
                order = Order.getInstance();
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
            case R.id.beginTrip:
                btnBeginTrip.setEnabled(false);
                btnPauseTrip.setEnabled(true);
                btnEndTrip.setEnabled(true);
                btnPauseTrip.setText("ОЖИДАНИЕ");
                llMain.setVisibility(View.VISIBLE);
                SetDefaultValues();
                order.startPoint = new LatLng(location.getLatitude(), location.getLongitude());
                timerHandler.postDelayed(timerRunnable, 0);
                break;
            case R.id.pauseTrip:
                if (btnPauseTrip.getText() == "ОЖИДАНИЕ") {
                    pauseSessionTime = 0;
                    pauseStartTime = System.currentTimeMillis();
                    btnPauseTrip.setText("ПРОДОЛЖИТЬ");
                    tvPause.setVisibility(View.VISIBLE);
                    isPause = true;
                } else {
                    pauseTotalTime += pauseSessionTime;
                    btnPauseTrip.setText("ОЖИДАНИЕ");
                    tvPause.setVisibility(View.INVISIBLE);
                    isPause = false;
                }
                break;
            case R.id.endTrip:
                EndTrip();
                break;
        }
    }

    private void EndTrip() {
        ClearMapFromLines();
        btnBeginTrip.setEnabled(false);
        btnEndTrip.setEnabled(false);
        btnPauseTrip.setEnabled(false);
        tvPause.setVisibility(View.INVISIBLE);
        llMain.setVisibility(View.INVISIBLE);

        timerHandler.removeCallbacks(timerRunnable);
        Intent intent = new Intent(this, FinishOrder.class);
        Bundle bundle = new Bundle();
        bundle.putString("Distance", df.format(distance/1000));
        bundle.putString("Price", df.format(price));
        bundle.putString("Time", df.format(time));
        bundle.putString("BeginPoint", order.startPoint.toString());
        bundle.putString("EndPoint", order.endPoint.toString());
        bundle.putString("FeePrice", df.format(feePrice));
        bundle.putString("FeeTime", df.format(pauseTotalTime/60));
        intent.putExtras(bundle);
        startActivityForResult(intent, FINISH_ORDER_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        order = Order.getInstance();
        if (requestCode == FINISH_ORDER_ID) {
            Toast.makeText(getApplicationContext(), data.getExtras().getString("MESSAGE"), Toast.LENGTH_LONG).show();
            mMap.clear();
        }
        if (requestCode == MAKE_ORDER_ID) {
            if (order.id != 0) {
                Toast.makeText(getApplicationContext(), "Заказ выбран ", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Координаты начала пути получены " + order.startPoint);
                btnBeginTrip.setEnabled(true);
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(order.startPoint).title("Здесь ваш клиент"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(order.startPoint, 15));
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
        startMain.addCategory( Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
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
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_order:
                if (order.id == 0) {
                    OpenOrder();
                    return true;
                }
                return false;
            case R.id.action_settings:
                OpenSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void OpenOrder() {
        Intent intent = new Intent(this, OrderActivity.class);
        startActivityForResult(intent, MAKE_ORDER_ID);
    }

    private void OpenSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
