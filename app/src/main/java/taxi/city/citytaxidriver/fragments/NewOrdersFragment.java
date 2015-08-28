package taxi.city.citytaxidriver.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.adapters.NewOrderAdapter;
import taxi.city.citytaxidriver.db.models.OrderModel;
import taxi.city.citytaxidriver.models.GlobalSingleton;
import taxi.city.citytaxidriver.models.Order;
import taxi.city.citytaxidriver.models.OrderStatus;
import taxi.city.citytaxidriver.networking.RestClient;
import taxi.city.citytaxidriver.utils.Constants;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewOrdersFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout swipeLayout;
    RecyclerView rvOrders;

    NewOrderAdapter newOrderAdapter;

    public NewOrdersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        rvOrders = (RecyclerView)view.findViewById(R.id.rvOrders);
        rvOrders.setHasFixedSize(true);

        newOrderAdapter = new NewOrderAdapter(new ArrayList<OrderModel>(), R.layout.cardview_order, getActivity());

        rvOrders.setAdapter(newOrderAdapter);
        rvOrders.setItemAnimator(new DefaultItemAnimator());
        rvOrders.setLayoutManager(new LinearLayoutManager(getActivity()));

        newOrderAdapter.setDataset(GlobalSingleton.getInstance().newOrders);

        return view;
    }

    private void fetchNewOrders() {
        RestClient.getOrderService().getAllByDistance(OrderStatus.NEW, Constants.ORDER_SEARCH_RANGE, new Callback<ArrayList<OrderModel>>() {
            @Override
            public void success(ArrayList<OrderModel> orders, Response response) {
                newOrderAdapter.setDataset(orders);
                GlobalSingleton.getInstance().newOrders = (orders);
                swipeLayout.setRefreshing(false);
            }

            @Override
            public void failure(RetrofitError error) {
                swipeLayout.setRefreshing(false);
                Toast.makeText(getActivity(), getString(R.string.error_unable_to_get_data_from_the_server), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRefresh() {
        fetchNewOrders();
    }
}
