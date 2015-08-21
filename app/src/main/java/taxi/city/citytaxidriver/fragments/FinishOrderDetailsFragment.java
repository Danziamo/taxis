package taxi.city.citytaxidriver.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import taxi.city.citytaxidriver.core.Client;
import taxi.city.citytaxidriver.core.Order;
import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.enums.OStatus;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.nerworking.ApiService;
import taxi.city.citytaxidriver.utils.Helper;

public class FinishOrderDetailsFragment extends Fragment implements View.OnClickListener {

    private Client mClient;
    private Order order;
    private FinishOrderTask finishTask = null;
    private SweetAlertDialog pDialog;

    private Button btnMap;
    private Button btnWait;
    private Button btnFinish;

    public FinishOrderDetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_finish_order_details, container, false);
        order = Order.getInstance();

        Intent intent = getActivity().getIntent();
        mClient = (Client)intent.getExtras().getSerializable("DATA");

        EditText etAddressStart = (EditText)rootView.findViewById(R.id.editTextStartAddress);
        TextView tvWaitTime = (TextView)rootView.findViewById(R.id.textViewWaitingTime);
        TextView tvWaitSum = (TextView)rootView.findViewById(R.id.textViewWaitingSum);
        TextView tvDistance = (TextView)rootView.findViewById(R.id.textViewDistance);
        TextView tvSum = (TextView)rootView.findViewById(R.id.textViewSum);
        TextView tvTotalSum = (TextView)rootView.findViewById(R.id.textViewTotalSum);
        TextView tvFixedPrice = (TextView)rootView.findViewById(R.id.textViewFixedPrice);
        EditText etAddressStop = (EditText)rootView.findViewById(R.id.editTextStopAddress);
        LinearLayout llFixedPrice = (LinearLayout)rootView.findViewById(R.id.linearLayoutFixedPrice);
        llFixedPrice.setVisibility(View.GONE);

        double totalSum = 0;
        double waitSum = 0;
        double sum = 0;
        try {
            waitSum = Double.valueOf(mClient.waitSum);
            sum = Double.valueOf(mClient.sum);
            totalSum = waitSum + sum;
        } catch (Exception e) {
            totalSum = 0;
        }

        String waitTime = mClient.waitTime;
        if (waitTime.length() > 5) {
            waitTime = waitTime.substring(0, waitTime.length() - 3);
        }

        etAddressStart.setText(mClient.addressStart);
        tvWaitTime.setText(waitTime);
        tvWaitSum.setText(String.valueOf((int)waitSum));
        tvDistance.setText(mClient.distance);
        tvSum.setText(String.valueOf((int)sum));
        tvTotalSum.setText(String.valueOf((int)totalSum));
        etAddressStop.setText(mClient.addressEnd);

        etAddressStart.setEnabled(false);
        etAddressStop.setEnabled(false);

        double fixedPrice;
        try {
            fixedPrice = Double.valueOf(mClient.fixedPrice);
        } catch (Exception e) {
            fixedPrice = 0;
        }

        if (fixedPrice >= 50) {
            tvFixedPrice.setText((int)fixedPrice + " сом");
            llFixedPrice.setVisibility(View.VISIBLE);
        }

        btnMap = (Button)rootView.findViewById(R.id.buttonMap);
        btnWait = (Button)rootView.findViewById(R.id.buttonWait);
        btnFinish = (Button)rootView.findViewById(R.id.buttonFinish);

        btnMap.setOnClickListener(this);
        btnFinish.setOnClickListener(this);
        btnWait.setOnClickListener(this);

        updateViews();

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonMap:
                getActivity().finish();
                break;
            case R.id.buttonWait:
                waitOrder();
                break;
            case R.id.buttonFinish:
                finishOrder();
                break;
        }
    }

    private void finishOrder() {
        if (mClient.active) {
            sendPostRequest();
        } else {
            Intent intent = new Intent();
            intent.putExtra("returnCode", false);
            getActivity().setResult(2, intent);
            getActivity().finish();
        }
    }

    private void waitOrder() {

    }

    private void updateViews() {
        if (mClient.status.equals(OStatus.FINISHED.toString())) {
            btnMap.setVisibility(View.GONE);
            btnWait.setVisibility(View.GONE);
            btnMap.setVisibility(View.INVISIBLE);
            btnFinish.setText("Назад");
            btnFinish.setVisibility(View.GONE);
        } else if (mClient.status.equals(OStatus.PENDING.toString())) {
            btnFinish.setText("Доставил");
            btnWait.setText("Продолжить");
            btnWait.setVisibility(View.VISIBLE);
            btnMap.setVisibility(View.VISIBLE);
        } else if (mClient.status.equals(OStatus.ONTHEWAY.toString())) {
            btnFinish.setText("Доставил");
            btnWait.setText("Ожидание");
            btnWait.setVisibility(View.VISIBLE);
            btnMap.setVisibility(View.VISIBLE);
        }
    }

    private void sendPostRequest() {
        if (finishTask != null) {
            return;
        }

        showProgress(true);
        finishTask = new FinishOrderTask();
        finishTask.execute((Void) null);
    }



    public class FinishOrderTask extends AsyncTask<Void, Void, JSONObject> {

        FinishOrderTask() {}

        @Override
        protected JSONObject doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.

            order.status = OStatus.FINISHED;
            mClient.status = OStatus.FINISHED.toString();
            JSONObject data = new JSONObject();
            try {
                data =  mClient.getClientAsJSON();
                data.put("status", mClient.status);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ApiService.getInstance().patchRequest(data, "orders/" + mClient.id + "/");
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            finishTask = null;
            showProgress(false);
            try {
                if (Helper.isSuccess(result)) {
                    Intent intent = new Intent();
                    intent.putExtra("returnCode", true);
                    Helper.destroyOrderPreferences(getActivity(), User.getInstance().id);
                    order.clear();
                    getActivity().setResult(2, intent);
                    getActivity().finish();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("returnCode", false);
                    Helper.saveOrderPreferences(getActivity(), order);
                    getActivity().setResult(2, intent);
                    getActivity().finish();
                }
            } catch (JSONException ignored) {}
        }

        @Override
        protected void onCancelled() {
            finishTask = null;
        }
    }

    public void showProgress(final boolean show) {
        if (show) {
            pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper()
                    .setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Обновление");
            pDialog.setCancelable(true);
            pDialog.show();
        } else {
            pDialog.dismissWithAnimation();
        }
    }
}