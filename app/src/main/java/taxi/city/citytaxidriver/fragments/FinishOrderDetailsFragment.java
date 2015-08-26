package taxi.city.citytaxidriver.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.models.GlobalSingleton;
import taxi.city.citytaxidriver.models.Order;
import taxi.city.citytaxidriver.networking.RestClient;
import taxi.city.citytaxidriver.db.models.OrderModel;

public class FinishOrderDetailsFragment extends Fragment {

    private Button btnSubmit;
    private Order mOrder;

    public FinishOrderDetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_finish_order_details, container, false);

        Intent intent = getActivity().getIntent();
        mOrder = (Order)intent.getExtras().getSerializable("DATA");

        EditText etAddressStart = (EditText)rootView.findViewById(R.id.etStartAddress);
        TextView tvPhone = (TextView)rootView.findViewById(R.id.tvPhone);
        TextView tvWaitTime = (TextView)rootView.findViewById(R.id.textViewWaitingTime);
        TextView tvWaitSum = (TextView)rootView.findViewById(R.id.textViewWaitingSum);
        TextView tvDistance = (TextView)rootView.findViewById(R.id.textViewDistance);
        TextView tvSum = (TextView)rootView.findViewById(R.id.textViewSum);
        TextView tvTotalSum = (TextView)rootView.findViewById(R.id.textViewTotalSum);
        Button btnSubmit = (Button)rootView.findViewById(R.id.btnSubmit);

        String waitTime = mOrder.getWaitTime();
        if (waitTime.length() > 5) {
            waitTime = waitTime.substring(0, waitTime.length() - 3);
        }

        etAddressStart.setText(mOrder.getStartName());
        tvWaitTime.setText(waitTime);
        tvWaitSum.setText(String.valueOf((int) mOrder.getWaitTimePrice()));
        tvDistance.setText(String.valueOf(mOrder.getDistance()));
        tvSum.setText(String.valueOf((int) mOrder.getTravelSum()));
        tvTotalSum.setText(String.valueOf((int) mOrder.getTotalSum()));

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishOrder(mOrder);
            }
        });


        return rootView;
    }

    private void finishOrder(Order order) {
        RestClient.getOrderService().update(order.getId(), new OrderModel(order), new Callback<Order>() {
            @Override
            public void success(Order order, Response response) {
                GlobalSingleton.getInstance(getActivity()).currentOrder = null;
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }

            @Override
            public void failure(RetrofitError error) {
                getActivity().setResult(Activity.RESULT_FIRST_USER);
                getActivity().finish();
            }
        });
    }
}