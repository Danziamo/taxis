package taxi.city.citytaxidriver.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.models.GlobalSingleton;
import taxi.city.citytaxidriver.models.Order;
import taxi.city.citytaxidriver.models.OrderStatus;
import taxi.city.citytaxidriver.models.User;
import taxi.city.citytaxidriver.networking.RestClient;
import taxi.city.citytaxidriver.networking.model.NOrder;
import taxi.city.citytaxidriver.utils.Constants;

public class MapsFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap mGoogleMap;

    Order mOrder;
    User mUser;

    public MapsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_maps, container, false);

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
        RestClient.getOrderService().update(mOrder.getId(), new NOrder(), new Callback<Order>() {
            @Override
            public void success(Order order, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void getNewOrders() {
        RestClient.getOrderService().getAllByDistance(OrderStatus.NEW, Constants.ORDER_SEARCH_RANGE, new Callback<ArrayList<Order>>() {
            @Override
            public void success(ArrayList<Order> orders, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void getSosOrders() {
        RestClient.getOrderService().getAllByStatus(OrderStatus.SOS, new Callback<ArrayList<Order>>() {
            @Override
            public void success(ArrayList<Order> orders, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
}
