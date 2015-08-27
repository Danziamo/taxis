package taxi.city.citytaxidriver.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import taxi.city.citytaxidriver.BuildConfig;
import taxi.city.citytaxidriver.FinishOrderDetailsActivity;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.db.models.OrderModel;
import taxi.city.citytaxidriver.db.models.Tariff;
import taxi.city.citytaxidriver.models.GlobalSingleton;
import taxi.city.citytaxidriver.models.Order;
import taxi.city.citytaxidriver.models.OrderStatus;
import taxi.city.citytaxidriver.models.User;
import taxi.city.citytaxidriver.networking.RestClient;
import taxi.city.citytaxidriver.networking.model.BOrder;
import taxi.city.citytaxidriver.utils.Constants;
import taxi.city.citytaxidriver.utils.Helper;

public class MapsFragment extends Fragment implements GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private MapView mMapView;
    private GoogleMap mGoogleMap;

    TextView tvAddress;
    TextView tvPrice;
    TextView tvStatus;

    LinearLayout llOrderDetails;
    TextView tvWaitTime;
    TextView tvWaitSum;
    TextView tvDistance;
    TextView tvTravelSum;

    Button btnLeft;
    Button btnRight;
    Button btnCenter;

    private HashMap<Marker, OrderModel> mNewOrderMarkerMap;
    private HashMap<Marker, Order> mSosOrderMarkerMap;

    OrderModel mOrderModel;

    User mUser;
    private Location prevLocation;
    private long pauseStartTime;
    private long timerStartTime;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            if (mOrderModel == null) {
                timerHandler.removeCallbacks(timerRunnable);
                return;
            }
            long millis = System.currentTimeMillis() - timerStartTime;
            double seconds = (double) (millis / 1000);
            mOrderModel.setDuration((long) seconds);
            updateCounterViews();

            if ((int)seconds % 10 < 1 && mOrderModel != null) {
                mOrderModel.save();
            }
            timerHandler.postDelayed(this, 1000);
        }
    };

    public MapsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_maps, container, false);

        mNewOrderMarkerMap = new HashMap<>();
        mSosOrderMarkerMap = new HashMap<>();

        mOrderModel = GlobalSingleton.getInstance().currentOrderModel;
        mUser = GlobalSingleton.getInstance(getActivity()).currentUser;

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        mGoogleMap = mMapView.getMap();
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setOnMarkerClickListener(this);

        tvAddress = (TextView)view.findViewById(R.id.tvAddress);
        tvPrice = (TextView)view.findViewById(R.id.tvPrice);
        tvStatus = (TextView)view.findViewById(R.id.tvStatus);

        llOrderDetails = (LinearLayout)view.findViewById(R.id.llOrderDetails);
        tvWaitTime = (TextView)view.findViewById(R.id.tvWaitTime);
        tvWaitSum = (TextView)view.findViewById(R.id.tvWaitSum);
        tvDistance = (TextView)view.findViewById(R.id.tvDistance);
        tvTravelSum = (TextView)view.findViewById(R.id.tvTravelSum);

        btnLeft = (Button)view.findViewById(R.id.btnLeft);
        btnRight = (Button)view.findViewById(R.id.btnRight);
        btnCenter = (Button)view.findViewById(R.id.btnCenter);

        btnLeft.setOnClickListener(this);
        btnRight.setOnClickListener(this);
        btnCenter.setOnClickListener(this);

        getNewOrders();
        getSosOrders();

        if (mOrderModel != null) {
            timerHandler.post(timerRunnable);
        }
        updateCounterViews();
        updateFooter();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    private void createBortOrder() {
        if(Tariff.isTariffsUpToDate()){
            Tariff tariff = Tariff.getTariffById(Constants.DEFAULT_BORT_TARIFF);
            createBortOrder(tariff);
        }else{
            RestClient.getOrderService().getAllTariffs(new Callback<List<Tariff>>() {
                @Override
                public void success(List<Tariff> tariffs, Response response) {
                    Tariff.upgradeTariffs(tariffs);
                    Tariff tariff = Tariff.getTariffById(Constants.DEFAULT_BORT_TARIFF);
                    createBortOrder(tariff);
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getActivity(), "Failed to fetch tariff", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void startTimer(long shift) {
        timerStartTime = System.currentTimeMillis() - shift;
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void createBortOrder(Tariff tariff){
        //@TODO try send query with OrderModel
        final OrderModel order = new OrderModel();
        order.setDriverId(mUser.getId());
        order.setStatus(OrderStatus.ONTHEWAY);
        order.setStartName("");
        order.setStopName("");
        order.setStartPoint(Helper.getFormattedLatLng(GlobalSingleton.getInstance(getActivity()).curPosition));
        order.setStopPoint(Helper.getFormattedLatLng(GlobalSingleton.getInstance(getActivity()).curPosition));
        order.setClientPhone(mUser.getPhone());
        order.setTariffId(tariff.getTariffId());
        RestClient.getOrderService().createOrder(new BOrder(order), new Callback<OrderModel>() {
            @Override
            public void success(OrderModel orderModel, Response response) {
                startTimer(0);
                mOrderModel = orderModel;
                GlobalSingleton.getInstance(getActivity()).currentOrderModel = mOrderModel;
                updateFooter();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Failed to create order", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateOrder() {
        RestClient.getOrderService().update(mOrderModel.getOrderId(), mOrderModel, new Callback<OrderModel>() {
            @Override
            public void success(OrderModel orderModel, Response response) {
                //@TODO neet to test in release mode
                if (BuildConfig.DEBUG) {
                    Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (BuildConfig.DEBUG) {
                    Toast.makeText(getActivity(), "Failure", Toast.LENGTH_SHORT).show();
                }
            }
        });

        updateFooter();
    }

    private void finishOrder() {
        /*mOrder.setStatus(OrderStatus.FINISHED);
        mOrder.setStopPoint(GlobalSingleton.getInstance(getActivity()).getPosition());
        updateOrder();*/

        Intent intent = new Intent(getActivity(), FinishOrderDetailsActivity.class);
        intent.putExtra("DATA", mOrderModel);
        startActivityForResult(intent, Constants.FINISH_ORDER_KEY);
    }

    private void cancelOrder() {
        mOrderModel.setStatus(OrderStatus.NEW);
        mOrderModel.setStopPoint(null);
        updateOrder();
    }

    private void takeOrder(OrderModel order) {
        //TODO krutilka
        order.setDriverId(mUser.getId());
        order.setStopPoint(GlobalSingleton.getInstance(getActivity()).getPosition());
        RestClient.getOrderService().update(order.getOrderId(), order, new Callback<OrderModel>() {
            @Override
            public void success(OrderModel orderModel, Response response) {
                mOrderModel = orderModel;
                GlobalSingleton.getInstance(getActivity()).currentOrderModel = mOrderModel;
                startTimer(0);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Cannot take order", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void clearMapFromNewOrders() {
        for (int i = 0; i < mNewOrderMarkerMap.size(); ++i) {
            for(Map.Entry<Marker, OrderModel> entry : mNewOrderMarkerMap.entrySet()) {
                entry.getKey().remove();
            }
        }
        mNewOrderMarkerMap = new HashMap<>();
    }

    private void clearMapFromSosOrders() {
        for (int i = 0; i < mSosOrderMarkerMap.size(); ++i) {
            for(Map.Entry<Marker, Order> entry : mSosOrderMarkerMap.entrySet()) {
                entry.getKey().remove();
            }
        }
        mSosOrderMarkerMap = new HashMap<>();
    }

    public void getNewOrders() {
        if (mOrderModel != null) return;
        RestClient.getOrderService().getAllByDistance(OrderStatus.NEW, Constants.ORDER_SEARCH_RANGE, new Callback<ArrayList<OrderModel>>() {
            @Override
            public void success(ArrayList<OrderModel> orders, Response response) {
                GlobalSingleton.getInstance(getActivity()).newOrders = orders;
                clearMapFromNewOrders();
                for (int i = orders.size() - 1; i >= 0; i -= 1) {
                    OrderModel order = orders.get(i);
                    Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(order.getStartPointPosition())
                            .title(order.getStartName()));
                    mNewOrderMarkerMap.put(marker, order);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Error fetching new orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getSosOrders() {
        RestClient.getOrderService().getSosOrders(new Callback<ArrayList<Order>>() {
            @Override
            public void success(ArrayList<Order> orders, Response response) {
                clearMapFromSosOrders();
                for (int i = orders.size() - 1; i >= 0; i -= 1) {
                    Order order = orders.get(i);
                    Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(order.getStartPointPosition())
                            .title(order.getStartName()));
                    mSosOrderMarkerMap.put(marker, order);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void displayOrderOnMap(Order order) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(order.getStartPointPosition(), 17));
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17), 1000, null);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showOrderInfoDialog(mNewOrderMarkerMap.get(marker));
            }
        }, 1150);
        return true;
    }

    private void showOrderInfoDialog(final OrderModel order) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setTitle(getString(R.string.alert_info_order));
        dialog.setContentView(R.layout.dialog_order_info);

        TextView tvAddress = (TextView)dialog.findViewById(R.id.tvAddress);
        TextView tvClientPhone = (TextView)dialog.findViewById(R.id.tvPhone);
        TextView tvDescription = (TextView)dialog.findViewById(R.id.tvDescription);
        TextView tvFixedPrice = (TextView)dialog.findViewById(R.id.tvFixedPrice);
        TextView tvStopAddress = (TextView)dialog.findViewById(R.id.tvStopAddress);
        LinearLayout llFixedPrice = (LinearLayout)dialog.findViewById(R.id.llFixedPrice);
        TextView tvCounter = (TextView)dialog.findViewById(R.id.counterView);

        Button btnCancel = (Button)dialog.findViewById(R.id.btnCancel);
        Button btnSubmit = (Button)dialog.findViewById(R.id.btnSubmit);

        tvAddress.setText(order.getStartName());
        tvClientPhone.setText(order.getClientPhone());
        tvDescription.setText(order.getDescription());
        if (order.isFixedPrice()) {
            tvCounter.setVisibility(View.GONE);
            tvFixedPrice.setText(String.valueOf((int)order.getFixedPrice()));
            tvStopAddress.setText(order.getStopName());
        } else {
            llFixedPrice.setVisibility(View.GONE);
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(order.getStartPointPosition(), 14));
                dialog.dismiss();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeOrder(order);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void updateFooter() {
        if (mOrderModel == null) {
            btnLeft.setText(getString(R.string.s_borta));
            btnRight.setText(getString(R.string.orders));
            btnCenter.setText("ОНЛАЙН");
        } else if (mOrderModel.getStatus() == OrderStatus.ACCEPTED) {
            btnLeft.setText("На месте");
            btnRight.setText("Отказ");
            tvStatus.setText(mOrderModel.getStartName());
        } else if (mOrderModel.getStatus() == OrderStatus.WAITING) {
            btnLeft.setText("На борту");
            btnRight.setText("Отказ");
            tvStatus.setText("Ожидание");
        } else if (mOrderModel.getStatus() == OrderStatus.ONTHEWAY) {
            btnLeft.setText("Доставил");
            btnCenter.setText("Ожидание");
            tvStatus.setText("В пути");
            //TODO подумать
            btnRight.setText("Доп. инфо");
        } else if (mOrderModel.getStatus() == OrderStatus.PENDING) {
            btnLeft.setText("Доставил");
            btnRight.setText("Доп. инфо");
            btnCenter.setText("Продолжить");
            tvStatus.setText("Ожидание");
        } else if (mOrderModel.getStatus() == OrderStatus.FINISHED) {

        } else {
            btnLeft.setText(getString(R.string.s_borta));
            btnRight.setText(getString(R.string.orders));
            btnCenter.setText("ОНЛАЙН");
        }
    }

    private void updateCounterViews() {
        if (mOrderModel == null) {
            llOrderDetails.setVisibility(View.GONE);
        } else {
            llOrderDetails.setVisibility(View.VISIBLE);
            tvWaitTime.setText(String.valueOf(mOrderModel.getWaitTime()));
            tvWaitSum.setText(String.valueOf((int) mOrderModel.getWaitTimePrice()));
            tvDistance.setText(String.valueOf(mOrderModel.getDistance()));
            tvTravelSum.setText(String.valueOf((int)mOrderModel.getTravelSum()));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLeft:
                if (mOrderModel == null) {
                    createBortOrder();
                } else if (mOrderModel.getStatus() == OrderStatus.ACCEPTED) {

                } else if (mOrderModel.getStatus() == OrderStatus.WAITING) {

                } else if (mOrderModel.getStatus() == OrderStatus.ONTHEWAY) {
                    finishOrder();
                } else if (mOrderModel.getStatus() == OrderStatus.PENDING) {
                    finishOrder();
                } else {

                }
                updateFooter();
                break;
            case R.id.btnCenter:
                updateFooter();
                break;
            case R.id.btnRight:
                updateFooter();
                break;
            default:
                break;
        }
    }

    public void updateOrderDetails(Location location) {
        if (mOrderModel == null) return;
        if (prevLocation != null) {
            if(location.getTime() <= prevLocation.getTime()){
                return;
            }

            long timeDifference = (location.getTime() - prevLocation.getTime()) / 1000;
            float distance = location.distanceTo(prevLocation);
            if(distance / timeDifference > Constants.GPS_MAX_SPEED){
                return;
            }
            mOrderModel.setDistance(mOrderModel.getDistance() + distance/1000);
        }

        prevLocation = location;
        updateCounterViews();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constants.FINISH_ORDER_KEY){
            if (resultCode != Activity.RESULT_CANCELED) {
                if(resultCode == Activity.RESULT_FIRST_USER){
                    mOrderModel.save();
                }
                GlobalSingleton.getInstance().currentOrderModel = null;
                mOrderModel = null;
            }
            updateCounterViews();
            updateFooter();
        }
    }
}
