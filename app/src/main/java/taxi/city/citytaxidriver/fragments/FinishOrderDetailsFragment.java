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
import taxi.city.citytaxidriver.models.OrderStatus;
import taxi.city.citytaxidriver.networking.RestClient;
import taxi.city.citytaxidriver.db.models.OrderModel;

public class FinishOrderDetailsFragment extends Fragment {

    private OrderModel mOrderModel;
    private long id;

    public FinishOrderDetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_finish_order_details, container, false);

        Intent intent = getActivity().getIntent();
        id = intent.getExtras().getLong("DATA", 0);

        mOrderModel = OrderModel.getById(id);

        TextView etAddressStart = (TextView)rootView.findViewById(R.id.etStartAddress);
        TextView tvPhone = (TextView)rootView.findViewById(R.id.tvPhone);
        TextView tvWaitTime = (TextView)rootView.findViewById(R.id.textViewWaitingTime);
        TextView tvWaitSum = (TextView)rootView.findViewById(R.id.textViewWaitingSum);
        TextView tvDistance = (TextView)rootView.findViewById(R.id.textViewDistance);
        TextView tvSum = (TextView)rootView.findViewById(R.id.textViewSum);
        TextView tvTotalSum = (TextView)rootView.findViewById(R.id.textViewTotalSum);
        Button btnSubmit = (Button)rootView.findViewById(R.id.btnSubmit);

        String waitTime = mOrderModel.getWaitTime();
        if (waitTime.length() > 5) {
            waitTime = waitTime.substring(0, waitTime.length() - 3);
        }

        etAddressStart.setText(mOrderModel.getStartName());
        tvPhone.setText(mOrderModel.getClientPhone());
        tvWaitTime.setText(waitTime);
        tvWaitSum.setText(String.valueOf((int) mOrderModel.getWaitTimePrice()));
        tvDistance.setText(String.valueOf(mOrderModel.getDistance()));
        tvSum.setText(String.valueOf((int) mOrderModel.getTravelSum()));
        tvTotalSum.setText(String.valueOf((int) mOrderModel.getTotalSum()));

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOrderModel.setStatus(OrderStatus.FINISHED);
                finishOrder(mOrderModel);
            }
        });


        return rootView;
    }

    private void finishOrder(OrderModel order) {
        RestClient.getOrderService().update(order.getOrderId(), order, new Callback<OrderModel>() {
            @Override
            public void success(OrderModel order, Response response) {
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }

            @Override
            public void failure(RetrofitError error) {
                mOrderModel.save();
                getActivity().setResult(Activity.RESULT_FIRST_USER);
                getActivity().finish();
            }
        });
    }
}