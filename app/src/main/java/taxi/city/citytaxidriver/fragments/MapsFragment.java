package taxi.city.citytaxidriver.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import taxi.city.citytaxidriver.BuildConfig;
import taxi.city.citytaxidriver.FinishOrderDetailsActivity;
import taxi.city.citytaxidriver.NewOrdersActivity;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.db.models.OrderModel;
import taxi.city.citytaxidriver.db.models.Tariff;
import taxi.city.citytaxidriver.models.GlobalSingleton;
import taxi.city.citytaxidriver.models.OnlineStatus;
import taxi.city.citytaxidriver.models.Order;
import taxi.city.citytaxidriver.models.OrderStatus;
import taxi.city.citytaxidriver.models.User;
import taxi.city.citytaxidriver.networking.RestClient;
import taxi.city.citytaxidriver.networking.model.BOrder;
import taxi.city.citytaxidriver.utils.Constants;
import taxi.city.citytaxidriver.utils.Helper;
import taxi.city.citytaxidriver.views.OrderInfoDialog;

public class MapsFragment extends BaseFragment implements GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private MapView mMapView;
    private GoogleMap mGoogleMap;

    TextView tvAddress;
    TextView tvPrice;
    TextView tvStatus;

    RelativeLayout lockerLayout;
    LinearLayout llOrderDetails;
    TextView tvWaitTime;
    TextView tvWaitSum;
    TextView tvDistance;
    TextView tvTravelSum;

    TextView btnLeft;
    TextView btnRight;
    Button btnCenter;

    private HashMap<Marker, OrderModel> mNewOrderMarkerMap;
    private HashMap<Marker, Order> mSosOrderMarkerMap;

    OrderModel mOrderModel;

    User mUser;
    private Location prevLocation;
    private long timerStartTime;

    private OrderInfoDialog orderInfoDialog;

    Handler globalTimerHandler = new Handler();
    Runnable globalTimerRunnable = new Runnable() {

        @Override
        public void run() {
            getNewOrders();
            getSosOrders();
            globalTimerHandler.postDelayed(this, 30 * 1000);
        }
    };

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            if (mOrderModel == null) {
                timerHandler.removeCallbacks(timerRunnable);
                return;
            }
            long currentTimeMillis = System.currentTimeMillis();
            long millis = currentTimeMillis - timerStartTime;
            double seconds = (double) (millis / 1000);
            mOrderModel.setDuration((long) seconds);

            if (mOrderModel.getStatus() == OrderStatus.WAITING || mOrderModel.getStatus() == OrderStatus.PENDING) {
                mOrderModel.setPauseDuration(mOrderModel.getPauseDuration() + 1);
            }
            updateCounterViews();

            if ((int)seconds % 10 < 1 && mOrderModel != null) {
                mOrderModel.save();
                updateOrder();
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
        lockerLayout = (RelativeLayout)view.findViewById(R.id.locker);

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

        btnLeft = (TextView)view.findViewById(R.id.btnLeft);
        btnRight = (TextView)view.findViewById(R.id.btnRight);
        btnCenter = (Button)view.findViewById(R.id.btnCenter);

        btnLeft.setOnClickListener(this);
        btnRight.setOnClickListener(this);
        btnCenter.setOnClickListener(this);

        globalTimerHandler.postDelayed(globalTimerRunnable, 0);

        if (mOrderModel != null) {
            startTimer(Helper.getLongFromString(mOrderModel.getOrderTravelTime()));
        }
        updateCounterViews();
        updateFooter();

        orderInfoDialog = new OrderInfoDialog(getActivity());
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
                    if (error.getKind() == RetrofitError.Kind.HTTP) {
                        if (error.getResponse().getStatus() != HttpURLConnection.HTTP_INTERNAL_ERROR) {
                            String detail = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                            String displayMessage = "Заказ отменён или занят";
                            if (detail.toLowerCase().contains("user have not enough money")) {
                                displayMessage = "Не достатончно денег на балансе";
                            } else if (detail.toLowerCase().contains("canceled")) {
                                displayMessage = "Заказ отменен клиентом";
                            }
                            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText(displayMessage)
                                    .setContentText("")
                                    .setConfirmText("Ок")
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismissWithAnimation();
                                        }
                                    })
                                    .show();
                            mOrderModel = null;
                            GlobalSingleton.getInstance(getActivity()).currentOrderModel = null;
                        }
                    }
                }
            });
        }

    }

    private void startTimer(long shift) {
        timerStartTime = System.currentTimeMillis() - shift*1000;
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
                clearMapFromNewOrders();
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
                hideProgress();
                if (BuildConfig.DEBUG) {
                    Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                if (BuildConfig.DEBUG) {
                    Toast.makeText(getActivity(), "Failure", Toast.LENGTH_SHORT).show();
                }

                if (error.getKind() == RetrofitError.Kind.HTTP) {
                    if (error.getResponse().getStatus() != HttpURLConnection.HTTP_INTERNAL_ERROR) {
                        String detail = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                        String displayMessage = "Заказ отменён или занят";
                        if (detail.toLowerCase().contains("user have not enough money")) {
                            displayMessage = "Не достатончно денег на балансе";
                        } else if (detail.toLowerCase().contains("canceled")) {
                            displayMessage = "Заказ отменен клиентом";
                        }
                        new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText(displayMessage)
                                .setContentText("")
                                .setConfirmText("Ок")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismissWithAnimation();
                                    }
                                })
                                .show();
                        mOrderModel = null;
                        GlobalSingleton.getInstance(getActivity()).currentOrderModel = null;
                    }
                }
            }
        });

        updateFooter();
    }

    private void finishOrder() {
        mOrderModel.save();
        Intent intent = new Intent(getActivity(), FinishOrderDetailsActivity.class);
        intent.putExtra("DATA", mOrderModel.getId());
        startActivityForResult(intent, Constants.FINISH_ORDER_KEY);
    }

    private void cancelOrder() {
        mOrderModel.setStatus(OrderStatus.NEW);
        mOrderModel.setStopPoint(null);
        showProgress("Обновление");
        RestClient.getOrderService().cancelOrder(mOrderModel.getOrderId(), OrderStatus.NEW, mUser.getId(), new Callback<OrderModel>() {
            @Override
            public void success(OrderModel orderModel, Response response) {
                hideProgress();
                mOrderModel = null;
                GlobalSingleton.getInstance(getActivity()).currentOrderModel = null;
                updateFooter();
                updateCounterViews();
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                Toast.makeText(getActivity(), "FAILED TO CANCEL ORDER", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void takeOrder(OrderModel order) {
        order.setDriverId(mUser.getId());
        order.setStopPoint(GlobalSingleton.getInstance(getActivity()).getPosition());
        order.setStatus(OrderStatus.ACCEPTED);
        RestClient.getOrderService().update(order.getOrderId(), order, new Callback<OrderModel>() {
            @Override
            public void success(OrderModel orderModel, Response response) {
                clearMapFromNewOrders();
                mOrderModel = orderModel;
                GlobalSingleton.getInstance(getActivity()).currentOrderModel = mOrderModel;
                startTimer(0);
                updateFooter();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Cannot take order", Toast.LENGTH_SHORT).show();
                if (error.getKind() == RetrofitError.Kind.HTTP) {
                    if (error.getResponse().getStatus() != HttpURLConnection.HTTP_INTERNAL_ERROR) {
                        String detail = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                        String displayMessage = "Заказ отменён или занят";
                        if (detail.toLowerCase().contains("user have not enough money")) {
                            displayMessage = "Не достатончно денег на балансе";
                        } else if (detail.toLowerCase().contains("canceled")) {
                            displayMessage = "Заказ отменен клиентом";
                        }
                        new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText(displayMessage)
                                .setContentText("")
                                .setConfirmText("Ок")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismissWithAnimation();
                                    }
                                })
                                .show();
                        mOrderModel = null;
                        GlobalSingleton.getInstance(getActivity()).currentOrderModel = null;
                    }
                }
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
                updateNewOrderMarkers();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Error fetching new orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNewOrderMarkers(){
        ArrayList<OrderModel> orders = GlobalSingleton.getInstance().newOrders;
        clearMapFromNewOrders();
        for (int i = orders.size() - 1; i >= 0; i -= 1) {
            OrderModel order = orders.get(i);
            Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(order.getStartPointPosition())
                    .title(order.getStartName()));
            mNewOrderMarkerMap.put(marker, order);
        }
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
        if(orderInfoDialog.isShowCalled()){
            return true;
        }
        orderInfoDialog.setShowCalled(true);
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
        orderInfoDialog.setOrder(order, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btnCancel) {
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(order.getStartPointPosition(), 14));
                    orderInfoDialog.dismiss();
                } else {
                    takeOrder(order);
                    orderInfoDialog.dismiss();
                }
            }
        });
    }

    private void updateFooter() {
        if (mOrderModel == null) {
            btnLeft.setText(getString(R.string.s_borta));
            btnRight.setText(getString(R.string.orders));
            updateUserOnlineStatusView();
        } else if (mOrderModel.getStatus() == OrderStatus.ACCEPTED) {
            btnLeft.setText("НА МЕСТЕ");
            btnRight.setText("ОТКАЗ");
            tvStatus.setText(mOrderModel.getStartName());
        } else if (mOrderModel.getStatus() == OrderStatus.WAITING) {
            btnLeft.setText("НА БОРТУ");
            btnRight.setText("ОТКАЗ");
            tvStatus.setText("Ожидание");
        } else if (mOrderModel.getStatus() == OrderStatus.ONTHEWAY) {
            btnLeft.setText("ДОСТАВИЛ");
            btnCenter.setText("Ждать");
            tvStatus.setText("В пути");
            //TODO подумать
            btnRight.setText("Доп. инфо");
        } else if (mOrderModel.getStatus() == OrderStatus.PENDING) {
            btnLeft.setText("ДОСТАВИЛ");
            btnRight.setText("Доп. инфо");
            btnCenter.setText("В путь");
            tvStatus.setText("Ожидание");
        } else if (mOrderModel.getStatus() == OrderStatus.FINISHED) {

        } else {
            btnLeft.setText(getString(R.string.s_borta));
            btnRight.setText(getString(R.string.orders));
            updateUserOnlineStatusView();
        }
    }

    private void updateUserOnlineStatusView() {
        if(mUser.isOnline()) {
            btnCenter.setText("ОФФЛАЙН");
            tvStatus.setText(getString(R.string.you_are_online));
        }else{
            btnCenter.setText("ОНЛАЙН");
            tvStatus.setText(getString(R.string.you_are_offline));
        }
    }

    private void blockScreenAccordingToStatus(OnlineStatus status) {
        if (status != OnlineStatus.ONLINE) {
            lockerLayout.setVisibility(View.VISIBLE);
            btnLeft.setEnabled(true);
            btnRight.setEnabled(true);
        } else {
            btnLeft.setEnabled(false);
            btnRight.setEnabled(false);
            lockerLayout.setVisibility(View.GONE);
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
                    mOrderModel.setStatus(OrderStatus.WAITING);
                    updateOrder();
                } else if (mOrderModel.getStatus() == OrderStatus.WAITING) {
                    mOrderModel.setStatus(OrderStatus.ONTHEWAY);
                    updateOrder();
                } else if (mOrderModel.getStatus() == OrderStatus.ONTHEWAY) {
                    finishOrder();
                } else if (mOrderModel.getStatus() == OrderStatus.PENDING) {
                    finishOrder();
                } else {

                }
                updateFooter();
                break;
            case R.id.btnCenter:
                if(mOrderModel == null){
                    changeUserOnlineStatus();
                } else if (mOrderModel.getStatus() == OrderStatus.ONTHEWAY) {
                    mOrderModel.setStatus(OrderStatus.PENDING);
                    updateOrder();
                } else if (mOrderModel.getStatus() == OrderStatus.PENDING) {
                    mOrderModel.setStatus(OrderStatus.ONTHEWAY);
                    updateOrder();
                }
                
                updateFooter();
                break;
            case R.id.btnRight:
                if(mOrderModel == null){
                    if(orderInfoDialog.isShowCalled()){
                        orderInfoDialog.dismiss();
                    }
                    Intent intent = new Intent(getActivity(), NewOrdersActivity.class);
                    startActivityForResult(intent, Constants.NEW_ORDERS_KEY);
                } else if (mOrderModel.getStatus() == OrderStatus.ACCEPTED) {
                    cancelOrder();
                }
                updateFooter();
                break;
            default:
                break;
        }
    }

    public void updateOrderDetails(Location location) {
        if (mOrderModel == null) return;
        if (prevLocation != null && mOrderModel.getStatus() == OrderStatus.ONTHEWAY) {
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

    public void changeUserOnlineStatus(){

        if (!Helper.isNetworkAvailable(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.error_network_unavailable), Toast.LENGTH_SHORT).show();
            return;
        }

        final OnlineStatus newStatus = (mUser.isOnline())?OnlineStatus.OFFLINE:OnlineStatus.ONLINE;

        showProgress("Обновление");
        RestClient.getUserService().updateStatus(mUser.getId(), newStatus, new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                hideProgress();
                mUser.setOnlineStatus(newStatus);
                blockScreenAccordingToStatus(newStatus);
                GlobalSingleton.getInstance().currentUser = mUser;
                updateFooter();
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgress();
                Toast.makeText(getActivity(), getString(R.string.error_an_error_has_occurred_try_again), Toast.LENGTH_LONG).show();
                Crashlytics.logException(error);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constants.FINISH_ORDER_KEY){
            if (resultCode != Activity.RESULT_CANCELED) {
                GlobalSingleton.getInstance().currentOrderModel = null;
                mOrderModel = null;
            }
            updateCounterViews();
            updateFooter();
            getNewOrders();
        }else if(requestCode == Constants.NEW_ORDERS_KEY){
            if(orderInfoDialog.isShowCalled() || orderInfoDialog.isShowing()){
                orderInfoDialog.dismiss();
            }
            if(resultCode == Activity.RESULT_OK){
                updateNewOrderMarkers();
                OrderModel orderModel = (OrderModel) data.getSerializableExtra("DATA");
                if(orderModel != null){
                    for(Map.Entry<Marker, OrderModel> entry : mNewOrderMarkerMap.entrySet()){
                        Marker marker  = entry.getKey();
                        OrderModel order = entry.getValue();
                        if(order.getOrderId() == orderModel.getOrderId()){
                            onMarkerClick(marker);
                            break;
                        }
                    }
                }
            }
        }
    }
}
