package taxi.city.citytaxidriver.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.adapters.OrderAdapter;
import taxi.city.citytaxidriver.models.GlobalSingleton;
import taxi.city.citytaxidriver.models.Order;
import taxi.city.citytaxidriver.models.OrderStatus;
import taxi.city.citytaxidriver.models.User;
import taxi.city.citytaxidriver.networking.RestClient;
import taxi.city.citytaxidriver.networking.model.NOrder;
import taxi.city.citytaxidriver.utils.Constants;

public class MapsFragment extends Fragment implements GoogleMap.OnMarkerClickListener {

    private MapView mMapView;
    private GoogleMap mGoogleMap;

    TextView tvAddress;
    TextView tvPrice;
    TextView tvStatus;


    private HashMap<Marker, Order> mNewOrderMarkerMap;
    private HashMap<Marker, Order> mSosOrderMarkerMap;

    Order mOrder;
    User mUser;

    public MapsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_maps, container, false);

        mNewOrderMarkerMap = new HashMap<>();
        mSosOrderMarkerMap = new HashMap<>();

        mOrder = GlobalSingleton.getInstance(getActivity()).currentOrder;
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


        getNewOrders();
        getSosOrders();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    public void cancelOrder() {
        RestClient.getOrderService().updateStatus(mOrder.getId(), OrderStatus.NEW, new Callback<Order>() {
            @Override
            public void success(Order order, Response response) {
                mOrder = null;
                GlobalSingleton.getInstance(getActivity()).currentOrder = null;
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Failed to cancel order", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateOrder() {
        RestClient.getOrderService().update(mOrder.getId(), new NOrder(mOrder), new Callback<Order>() {
            @Override
            public void success(Order order, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }


    private void clearMapFromNewOrders() {
        for (int i = 0; i < mNewOrderMarkerMap.size(); ++i) {
            for(Map.Entry<Marker, Order> entry : mNewOrderMarkerMap.entrySet()) {
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
        RestClient.getOrderService().getAllByDistance(OrderStatus.NEW, Constants.ORDER_SEARCH_RANGE, new Callback<ArrayList<Order>>() {
            @Override
            public void success(ArrayList<Order> orders, Response response) {
                GlobalSingleton.getInstance(getActivity()).newOrders = orders;
                clearMapFromNewOrders();
                for (int i = orders.size() - 1; i >= 0; i -= 1) {
                    Order order = orders.get(i);
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
        RestClient.getOrderService().getAllByStatus(OrderStatus.SOS, new Callback<ArrayList<Order>>() {
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
        }, 1250);
        return true;
    }

    private void showOrderInfoDialog(final Order order) {
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

    private void takeOrder(Order order) {
        //TODO krutilka
        order.setDriver(mUser);
        RestClient.getOrderService().update(order.getId(), new NOrder(order), new Callback<Order>() {
            @Override
            public void success(Order order, Response response) {
                mOrder = order;
                GlobalSingleton.getInstance(getActivity()).currentOrder = order;
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Cannot take order", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFooter() {

    }

    private void updateCounter() {
        if (mOrder != null) {

        }
    }
}
