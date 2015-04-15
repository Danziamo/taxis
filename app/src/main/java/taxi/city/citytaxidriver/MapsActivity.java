package taxi.city.citytaxidriver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
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

import taxi.city.citytaxidriver.Core.Order;
import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Enums.OrderStatus;
import taxi.city.citytaxidriver.Service.ApiService;

public class MapsActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

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
    TextView tvSpeed;
    TextView tvTime;
    TextView tvFeeTime;
    TextView tvFeePrice;
    TextView tvTotalSum;
    Location gLocation;
    Order order = Order.getInstance();
    ApiService api = ApiService.getInstance();
    User user = User.getInstance();

    Location prev;
    double distance;
    double price;
    double ratio = 10;
    double freeMeters = 2000;
    double startPrice = 60;
    double waitSum = 0;
    long time;

    Button btnBeginTrip;
    Button btnPauseTrip;
    Button btnEndTrip;
    Location location;
    List<Polyline> polylines = new ArrayList<>();

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    DecimalFormat df = new DecimalFormat("#.##");

    long startTime = 0;
    long pauseTotalTime = 0;
    long pauseSessionTime = 0;
    long pauseStartTime = 0;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            double seconds = (double) (millis / 1000);

            time = (long) seconds;
            order.time = time;
            if (seconds % 30 < 1) {
                SendPostRequest(order.status, order.id);
            }
            tvTime.setText("Время: " + getTimeFromLong(order.time));
            tvTotalSum.setText("Общая сумма: " + (order.sum + order.waitSum) + " сом");
            tvDistance.setText("Расстояние: " + df.format(distance / 1000) + "км");
            tvPrice.setText("Цена: " + df.format(price) + "сом");
            saveToPreferences();
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
        tvFeePrice.setText("Сумма ожидания: " + df.format(waitSum) + " сом");
        tvFeeTime.setText("Время ожидания: " + getTimeFromLong(pauseTotalTime + pauseSessionTime));
        order.waitTime = pauseTotalTime + pauseSessionTime;
        order.waitSum = waitSum;
        pauseHandler.postDelayed(this, 1000);
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
        order = Order.getInstance();
        if (order.id != 0) {
            getPreferences();
        }

        updateViews();

        if (order.id == 0) {
            SetDefaultValues();
        }

        SetLocationRequest();
    }

    private void saveToPreferences() {
        settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("orderDistance", (float)order.distance);
        editor.putInt("orderId", order.id);
        editor.putLong("orderTime", order.time);
        editor.putLong("orderWaitTime", order.waitTime);
        editor.putString("orderPhone", order.clientPhone);
        editor.putString("orderStartPoint", order.getFormattedStartPoint());
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

    private LatLng stringToLatLng(String s) {
        if (s == null || s.equals("null"))
            return null;
        String[] geo = s.replace("(", "").replace(")", "").split(" ");

        double latitude = Double.valueOf(geo[2].trim());
        double longitude = Double.valueOf(geo[3].trim());
        return new LatLng(latitude, longitude);
    }

    private void getPreferences() {
        settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.contains("orderId")) {
            String pStatus = settings.getString("orderStatus", "");
            if (pStatus.equals("ontheway"))
                order.status = OrderStatus.STATUS.WAITING;
            else
                order.setStatus(pStatus);
            order.id = settings.getInt("orderId", 0);
            order.clientPhone = settings.getString("orderPhone", null);
            order.time = settings.getLong("orderTime", 0);
            order.distance = 1000*(double)settings.getFloat("orderDistance", 0);
            order.waitTime = settings.getLong("orderWaitTime", 0);
            order.startPoint = stringToLatLng(settings.getString("orderStartPoint", null));
            if (mMap != null && order.startPoint != null && order.clientPhone != null) {
                mMap.addMarker(new MarkerOptions().position(order.startPoint).title(order.clientPhone));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(order.startPoint, 15));
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        final AlertDialog.Builder builder =
                                new AlertDialog.Builder(MapsActivity.this);
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
            distance = order.distance;
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
            tvSpeed.setText("Скорость: 0 км/ч");
            tvTime.setText("Время: " + getTimeFromLong(order.time));
            tvPrice.setText("Цена: " + df.format(price) + "сом");
            tvDistance.setText("Расстояние: " + df.format(distance / 1000) + "км");
            tvFeePrice.setText("Сумма ожидания: " + df.format(waitSum) + " сом");
            tvFeeTime.setText("Время ожидания: " + getTimeFromLong(pauseTotalTime + pauseSessionTime));
            tvTotalSum.setText("Общая сумма: " + (order.sum + order.waitSum) + " сом");
        }
    }

    private String getTimeFromLong(long seconds) {
        int hr = (int)seconds/3600;
        int rem = (int)seconds%3600;
        int mn = rem/60;
        int sec = rem%60;
        String hrStr = (hr<10 ? "0" : "")+hr;
        String mnStr = (mn<10 ? "0" : "")+mn;
        String secStr = (sec<10 ? "0" : "")+sec;
        return String.format("%s:%s:%s", hrStr, mnStr, secStr);
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
        tvPrice = (TextView) findViewById(R.id.textViewPrice);
        btnBeginTrip = (Button) findViewById(R.id.beginTrip);
        btnEndTrip = (Button) findViewById(R.id.endTrip);
        btnPauseTrip = (Button) findViewById(R.id.pauseTrip);
        llMain = (LinearLayout) findViewById(R.id.mainLayout);
        tvSpeed = (TextView) findViewById(R.id.textViewSpeed);
        tvTime = (TextView) findViewById(R.id.textViewTime);
        tvFeePrice = (TextView) findViewById(R.id.textViewFeePrice);
        tvFeeTime = (TextView) findViewById(R.id.textViewFeeTime);
        tvTotalSum = (TextView) findViewById(R.id.textViewMapsTotalSum);

        btnBeginTrip.setOnClickListener(this);
        btnEndTrip.setOnClickListener(this);
        btnPauseTrip.setOnClickListener(this);
    }

    private void updateViews() {
        if (order.id == 0 || order.status == OrderStatus.STATUS.NEW) {
            order.clear();
            mMap.clear();
            btnBeginTrip.setVisibility(View.GONE);
            btnEndTrip.setVisibility(View.GONE);
            btnPauseTrip.setVisibility(View.GONE);
            llMain.setVisibility(View.GONE);
        } else {
            if (order.status == OrderStatus.STATUS.ACCEPTED) {
                llMain.setVisibility(View.GONE);
                btnBeginTrip.setVisibility(View.VISIBLE);
                btnBeginTrip.setText("НА МЕСТЕ");
                btnEndTrip.setVisibility(View.VISIBLE);
                btnEndTrip.setText("ОТАКЗАТЬ");
                btnPauseTrip.setVisibility(View.GONE);
            } else if (order.status == OrderStatus.STATUS.INPLACE) {
                btnBeginTrip.setVisibility(View.VISIBLE);
                btnBeginTrip.setText("НАЧАТЬ");
                btnPauseTrip.setVisibility(View.GONE);
                btnEndTrip.setVisibility(View.GONE);
                llMain.setVisibility(View.GONE);
            } else if (order.status == OrderStatus.STATUS.WAITING) {
                btnBeginTrip.setVisibility(View.GONE);
                btnBeginTrip.setText("НАЧАТЬ");
                btnEndTrip.setVisibility(View.VISIBLE);
                btnEndTrip.setText("ЗАКОНЧИТЬ");
                btnPauseTrip.setVisibility(View.VISIBLE);
                btnPauseTrip.setText("ПРОДОЛЖИТЬ");
                llMain.setVisibility(View.VISIBLE);
            } else if (order.status == OrderStatus.STATUS.ONTHEWAY) {
                btnBeginTrip.setVisibility(View.GONE);
                btnEndTrip.setVisibility(View.VISIBLE);
                btnPauseTrip.setVisibility(View.VISIBLE);
                btnPauseTrip.setText("ОЖИДАЕНИЕ");
                btnEndTrip.setText("ЗАКОНЧИТЬ");
                llMain.setVisibility(View.VISIBLE);
            } else {
                mMap.clear();
                btnBeginTrip.setVisibility(View.GONE);
                btnEndTrip.setVisibility(View.GONE);
                btnPauseTrip.setVisibility(View.GONE);
                llMain.setVisibility(View.GONE);
            }
        }
    }

    private void SetDefaultValues() {
        startTime = System.currentTimeMillis();
        pauseTotalTime = 0;
        pauseSessionTime = 0;
        distance = 0;
        price = startPrice;
        order.sum = startPrice;
        order.waitTime = 0;
        order.time = 0;

        tvSpeed.setText("Скорость: 0 км/ч");
        tvDistance.setText("Расстояние: " + df.format(distance / 1000) + " км");
        tvPrice.setText("Цена: " + df.format(price) + " сом");
        tvTime.setText("Время: " + "00:00:00");
        tvFeePrice.setText(null);
        tvFeeTime.setText(null);
        tvTotalSum.setText("Общая цена: " + df.format(price + waitSum) + " сом");
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
        this.location = location;
        if (location != null)
            order.endPoint = new LatLng(location.getLatitude(), location.getLongitude());

        if (location != null) {
            gLocation = location;
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
        boolean ifSession = (order.id != 0 && order.status == OrderStatus.STATUS.ONTHEWAY);
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
                order.startPoint = new LatLng(location.getLatitude(), location.getLongitude());
                order.endPoint = new LatLng(location.getLatitude(), location.getLongitude());
                if (order.status == OrderStatus.STATUS.ACCEPTED) {
                    order.status = OrderStatus.STATUS.INPLACE;
                    SendPostRequest(OrderStatus.STATUS.INPLACE, order.id);
                } else if (order.status == OrderStatus.STATUS.INPLACE) {
                    SetDefaultValues();
                    order.status = OrderStatus.STATUS.ONTHEWAY;
                    SendPostRequest(OrderStatus.STATUS.ONTHEWAY, order.id);
                }
                updateViews();
                break;
            case R.id.pauseTrip:
                if (order.status == OrderStatus.STATUS.ONTHEWAY) {
                    order.status = OrderStatus.STATUS.WAITING;
                    SendPostRequest(OrderStatus.STATUS.WAITING, order.id);
                    pauseSessionTime = 0;
                    pauseStartTime = System.currentTimeMillis();
                    pauseHandler.postDelayed(pauseRunnable, 0);
                } else if (order.status == OrderStatus.STATUS.WAITING) {
                    order.status = OrderStatus.STATUS.ONTHEWAY;
                    SendPostRequest(OrderStatus.STATUS.ONTHEWAY, order.id);
                    pauseHandler.removeCallbacks(pauseRunnable);
                    pauseTotalTime += pauseSessionTime;
                }
                updateViews();
                break;
            case R.id.endTrip:
                if (order.status == OrderStatus.STATUS.ACCEPTED) {
                    order.status = OrderStatus.STATUS.NEW;
                    timerHandler.removeCallbacks(timerRunnable);
                    pauseHandler.removeCallbacks(pauseRunnable);
                    SendPostRequest(order.status, order.id);
                } else {
                    SendPostRequest(order.status, order.id);
                    EndTrip();
                }
                updateViews();
                break;
        }
    }

    private void EndTrip() {
        ClearMapFromLines();
        timerHandler.removeCallbacks(timerRunnable);
        pauseHandler.removeCallbacks(pauseRunnable);
        Intent intent = new Intent(this, FinishOrder.class);
        startActivityForResult(intent, FINISH_ORDER_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FINISH_ORDER_ID) {
            if (data != null) {
                if (data.getBooleanExtra("returnCode", false)) {
                    Toast.makeText(getApplicationContext(), "Заказ завершен", Toast.LENGTH_LONG).show();
                    resetPreferences();
                    clearPreferences();
                } else {
                    saveToPreferences();
                    order.clear();
                    Toast.makeText(getApplicationContext(), "Заказ завершен. Ошибка при отправке данных на сервер", Toast.LENGTH_LONG).show();
                }
            }
            updateViews();
            mMap.clear();
        }
        if (requestCode == MAKE_ORDER_ID) {
            if (data != null && data.getBooleanExtra("returnCode", false)) {
                timerHandler.postDelayed(timerRunnable, 0);
                Toast.makeText(getApplicationContext(), "Заказ выбран ", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Координаты начала пути получены " + order.startPoint);
                order.endPoint = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(order.startPoint).title(order.clientPhone));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(order.startPoint, 15));
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        final AlertDialog.Builder builder =
                                new AlertDialog.Builder(MapsActivity.this);
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
            updateViews();
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
        if (order.id == 0 || order.status == OrderStatus.STATUS.FINISHED) {
            if (ifPreferenceActive()) {
                getPreferences();
                SendPostRequest(order.status, order.id);
            }
        }
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

    private void SendPostRequest(OrderStatus.STATUS status, int orderId) {
        if (sendTask != null) {
            return;
        }

        if (location != null) {
            order.endPoint = new LatLng(location.getLatitude(), location.getLongitude());
        }

        sendTask = new SendPostRequestTask(status, orderId);
        sendTask.execute((Void) null);
    }

    private class SendPostRequestTask extends AsyncTask<Void, Void, JSONObject> {
        private OrderStatus.STATUS status;
        private int driver;
        private int mOrderId = 0;

        SendPostRequestTask(OrderStatus.STATUS type, int orderId) {
            status = type;
            driver = user.id;
            mOrderId = orderId;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.

            JSONObject data = new JSONObject();
            JSONObject result = null;
            try {
                if (status == OrderStatus.STATUS.NEW) {
                    data.put("status", status);
                    data.put("driver", JSONObject.NULL);
                } else {
                    String travelTime = status == OrderStatus.STATUS.ACCEPTED || status == OrderStatus.STATUS.INPLACE ? "00:00:00" : getTimeFromLong(order.time);
                    data.put("status", status);
                    data.put("driver", status == OrderStatus.STATUS.NEW ? JSONObject.NULL : driver);
                    data.put("order_sum", order.sum + order.waitSum);
                    data.put("address_stop", status == OrderStatus.STATUS.NEW ? JSONObject.NULL : order.getFormattedEndPoint());
                    data.put("wait_time", getTimeFromLong(order.waitTime));
                    data.put("order_distance", (double) Math.round(order.distance * 100) / 100);
                    data.put("order_travel_time", travelTime);
                }
                JSONObject checkObject = api.getOrderRequest(null, "orders/" + mOrderId + "/");
                if (checkObject != null && checkObject.getInt("status_code") == HttpStatus.SC_OK) {
                    String checkStatus = checkObject.getString("status");
                    if (checkStatus == null || checkStatus.equals(OrderStatus.STATUS.CANCELED.toString())) {
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
                    if (status == OrderStatus.STATUS.FINISHED || status == OrderStatus.STATUS.NEW) {
                        order.clear();
                        resetPreferences();
                        clearPreferences();
                    }
                } else if (result != null && result.getInt("status_code") == 999) {
                    Toast.makeText(getApplicationContext(), "Клиент отменил заказ", Toast.LENGTH_LONG).show();
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