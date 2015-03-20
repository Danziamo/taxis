package taxi.city.citytaxidriver;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static final int FINISH_ORDER_ID = 2;

    LinearLayout lMain;
    TextView textViewLocation;
    TextView textViewPrice;
    TextView textViewSpeed;
    TextView textViewTime;

    Location prev;
    double distance;
    double price;
    double ratio = 10;
    double freeMeters = 2000;
    double startPrice = 60;
    double time;

    Button buttonBeginTrip;
    Button buttonEndTrip;
    List<Polyline> polylines = new ArrayList<>();

    public static final String TAG = "taxi maps";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    DecimalFormat df = new DecimalFormat("#.##");

    long startTime = 0;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            double seconds = (double) (millis / 1000);
            double minutes = seconds / 60;

            time = minutes;
            textViewTime.setText("Время: " + df.format(time) + " мин");
            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        SetGooglePlayServices();

        Initialize();
        SetDefaultValues();

        SetLocationRequest();
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
                .setInterval(1 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    }

    private void Initialize() {
        distance = 0;
        price = startPrice;
        prev = null;
        textViewLocation = (TextView) findViewById(R.id.location);
        textViewPrice = (TextView) findViewById(R.id.price);
        buttonBeginTrip = (Button) findViewById(R.id.beginTrip);
        buttonEndTrip = (Button) findViewById(R.id.endTrip);
        lMain = (LinearLayout) findViewById(R.id.mainLayout);
        textViewSpeed = (TextView) findViewById(R.id.speed);
        textViewTime = (TextView) findViewById(R.id.time);

        buttonBeginTrip.setOnClickListener(this);
        buttonEndTrip.setOnClickListener(this);

    }

    private void SetDefaultValues() {
        startTime = System.currentTimeMillis();
        distance = 0;
        price = startPrice;
        textViewSpeed.setText("Скорость: 0 км/ч");
        textViewLocation.setText("Расстояние: " + df.format(distance / 1000) + " км");
        textViewPrice.setText("Цена: " + df.format(price) + " сом");
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
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Connected to Google Play services!
        // The good stuff goes here.
        Log.i(TAG, "Location services connected.");
        startLocationUpdates();

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            handleNewLocation(location);
        };
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        double speed = location.getSpeed();
        boolean ifSession = !buttonBeginTrip.isEnabled();
        int zoom = ifSession ? 17 : 15;
        int bearing = ifSession ? (int)location.getBearing() : 0;
        int tilt = ifSession ? 45 : 0;

        if (ifSession){
            distance += prev.distanceTo(location);
            if (distance >= freeMeters)
                price = startPrice +  ratio*distance/1000;
            textViewLocation.setText("Расстояние: " + df.format(distance/1000) + "км");
            textViewPrice.setText("Цена: " + df.format(price) + "сом");
            textViewSpeed.setText("Скорость: " + df.format(speed*3.6) + " км/ч");

            if (prev != null) {
                if (ifSession) {
                    Polyline line = mMap.addPolyline(new PolylineOptions()
                            .add(new LatLng(prev.getLatitude(), prev.getLongitude()), latLng)
                            .width(12)
                            .color(0x7F0000FF)
                            .geodesic(true));

                    polylines.add(line);
                }
            }
        }

        prev = location;

        CameraPosition cp = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .bearing(bearing)
                .tilt(tilt)
                .build();

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
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
        handleNewLocation(location);
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
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void RunAlertDialog()
    {
        AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
        alertDialog.setTitle("Поездка оконченна!");

        Context context = MapsActivity.this.getApplicationContext();
        LinearLayout adlayout = new LinearLayout(context);
        adlayout.setOrientation(LinearLayout.VERTICAL);
        int color = Color.BLACK;

        final TextView tvDistance = new TextView(context);
        tvDistance.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvDistance.setTextSize(15);
        tvDistance.setTextColor(color);
        tvDistance.setText("Вы проехали: " + df.format(distance/1000) + " км");
        adlayout.addView(tvDistance);

        final TextView tvPrice = new TextView(context);
        tvPrice.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvPrice.setTextSize(15);
        tvPrice.setTextColor(color);
        tvPrice.setText("Зачислено: " + df.format(price) + " сомов");
        adlayout.addView(tvPrice);

        final TextView tvTime = new TextView(context);
        tvTime.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTime.setTextSize(15);
        tvTime.setTextColor(color);
        tvTime.setText("Время поездки: " + df.format(time) + " мин.");
        adlayout.addView(tvTime);

        alertDialog.setView(adlayout);

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.beginTrip:
                buttonEndTrip.setEnabled(true);
                buttonBeginTrip.setEnabled(false);
                lMain.setVisibility(View.VISIBLE);
                SetDefaultValues();
                timerHandler.postDelayed(timerRunnable, 0);
                break;
            case R.id.endTrip:
                EndTrip();
                break;
        }
    }

    private void EndTrip() {
        ClearMapFromLines();
        buttonBeginTrip.setEnabled(true);
        buttonEndTrip.setEnabled(false);
        lMain.setVisibility(View.INVISIBLE);
        timerHandler.removeCallbacks(timerRunnable);
        Intent intent = new Intent(this, FinishOrder.class);
        Bundle bundle = new Bundle();
        bundle.putString("Distance", df.format(distance/1000));
        bundle.putString("Price", df.format(price));
        bundle.putString("Time", df.format(time));
        bundle.putString("BeginPoint", "какой то аддресс");
        bundle.putString("EndPoint", "конечный аддрес какой то");
        intent.putExtras(bundle);
        startActivityForResult(intent, FINISH_ORDER_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FINISH_ORDER_ID) {
            Toast.makeText(getApplicationContext(), "Заказ окончен", Toast.LENGTH_LONG).show();
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
        if (!buttonBeginTrip.isEnabled()) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.action_order:
                OpenOrder();
                return true;
            case R.id.action_settings:
                OpenSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void OpenOrder() {
        Intent intent = new Intent(this, OrderActivity.class);
        startActivity(intent);
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
