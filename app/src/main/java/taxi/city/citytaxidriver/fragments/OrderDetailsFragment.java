package taxi.city.citytaxidriver.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.core.Client;
import taxi.city.citytaxidriver.core.GlobalParameters;
import taxi.city.citytaxidriver.core.Order;
import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.enums.OStatus;
import taxi.city.citytaxidriver.service.ApiService;
import taxi.city.citytaxidriver.utils.Helper;

public class OrderDetailsFragment extends Fragment implements View.OnClickListener {
    private Client mClient;
    private boolean isActive;
    private ApiService api = ApiService.getInstance();
    private Order order = Order.getInstance();
    private User user = User.getInstance();
    private GlobalParameters gp = GlobalParameters.getInstance();
    private SendPostRequestTask mTask = null;
    SweetAlertDialog pDialog;

    TextView tvClientPhone;
    TextView tvClientPhoneLabel;
    ImageButton imgBtnCallClient;

    Button btnOk;
    Button btnCancel;
    Button btnMap;

    LinearLayout llBtnMap;

    public OrderDetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order_details_activity, container, false);
        mClient = (Client) getActivity().getIntent().getSerializableExtra("DATA");
        isActive = getActivity().getIntent().getBooleanExtra("ACTIVE", false);

        TextView tvAddressStart = (TextView) rootView.findViewById(R.id.textViewStartAddress);
        tvClientPhone = (TextView) rootView.findViewById(R.id.textViewClientPhone);
        tvClientPhoneLabel = (TextView) rootView.findViewById(R.id.textViewClientPhoneLabel);
        TextView tvAddressStop = (TextView) rootView.findViewById(R.id.textViewStopAddress);
        TextView tvDescription = (TextView) rootView.findViewById(R.id.textViewDescription);
        TextView tvFixedPrice = (TextView) rootView.findViewById(R.id.textViewFixedPrice);
        LinearLayout llFixedPrice = (LinearLayout) rootView.findViewById(R.id.linearLayoutFixedPrice);
        llBtnMap = (LinearLayout) rootView.findViewById(R.id.linearLayoutMapInfo);
        imgBtnCallClient = (ImageButton) rootView.findViewById(R.id.imageButtonCallClient);

        tvAddressStart.setText(mClient.addressStart);
        tvClientPhone.setText(mClient.phone);
        tvAddressStop.setText(mClient.addressEnd);
        tvDescription.setText(mClient.description);
        double fixedPrice = Helper.getDouble(mClient.fixedPrice);
        tvFixedPrice.setText((int) fixedPrice + " сом");
        if (fixedPrice < 50) llFixedPrice.setVisibility(View.GONE);

        btnOk = (Button) rootView.findViewById(R.id.buttonActionOk);
        btnCancel = (Button) rootView.findViewById(R.id.buttonActionCancel);
        btnMap = (Button) rootView.findViewById(R.id.buttonMapInfo);
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnMap.setOnClickListener(this);
        imgBtnCallClient.setOnClickListener(this);

        updateViews();

        return rootView;
    }

    private void updateViews() {
        if (mClient.status.equals(OStatus.NEW.toString())) {
            llBtnMap.setVisibility(View.GONE);
            btnOk.setText("Взять");
            btnCancel.setText("Заказы");
            tvClientPhone.setVisibility(View.GONE);
            tvClientPhoneLabel.setVisibility(View.GONE);
            imgBtnCallClient.setVisibility(View.GONE);
            btnCancel.setBackgroundResource(R.drawable.button_shape_yellow);
            btnCancel.setTextColor(getResources().getColor(R.color.blacktext2));
        } else if (mClient.status.equals(OStatus.ACCEPTED.toString())) {
            llBtnMap.setVisibility(View.VISIBLE);
            btnOk.setText("На месте");
            btnCancel.setText("Отказ");
            tvClientPhone.setVisibility(View.VISIBLE);
            tvClientPhoneLabel.setVisibility(View.VISIBLE);
            imgBtnCallClient.setVisibility(View.VISIBLE);
            btnCancel.setBackgroundResource(R.drawable.button_shape_red);
            btnCancel.setTextColor(getResources().getColor(R.color.white));
        } else {
            llBtnMap.setVisibility(View.VISIBLE);
            tvClientPhone.setVisibility(View.VISIBLE);
            tvClientPhoneLabel.setVisibility(View.VISIBLE);
            imgBtnCallClient.setVisibility(View.VISIBLE);
            btnOk.setText("На борту");
            btnCancel.setText("Отказ");
            btnCancel.setBackgroundResource(R.drawable.button_shape_red);
            btnCancel.setTextColor(getResources().getColor(R.color.white));
        }
    }

    @Override
    public void onClick(View v) {
        if (order == null || order.id == 0 || order.status == null) {
            if (mClient.status.equals(OStatus.ACCEPTED.toString())
                    || mClient.status.equals(OStatus.WAITING.toString())) {
                Toast.makeText(getActivity().getApplicationContext(), "Заказ уже выбран или отменён", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
        switch (v.getId()) {
            case R.id.buttonActionOk:
                if (mClient.status.equals(OStatus.NEW.toString())) {
                    showProgress(true);
                    SendPostRequest(OStatus.ACCEPTED);
                } else if (mClient.status.equals(OStatus.ACCEPTED.toString())) {
                    mClient.status = OStatus.WAITING.toString();
                    order.status = OStatus.WAITING;
                    SendPostRequest(OStatus.WAITING);
                } else if (mClient.status.equals(OStatus.WAITING.toString())) {
                    mClient.status = OStatus.ONTHEWAY.toString();
                    order.status = OStatus.ONTHEWAY;
                    SendPostRequest(OStatus.ONTHEWAY);
                }
                break;
            case R.id.buttonActionCancel:
                if (!mClient.status.equals(OStatus.NEW.toString())) cancelOrder();
                else getActivity().finish();
                break;
            case R.id.imageButtonCallClient:
                callClient();
                break;
            default:
                Intent intent = new Intent();
                intent.putExtra("returnCode", true);
                getActivity().setResult(isActive ? 3 : 1, intent);
                getActivity().finish();
        }
    }

    private void callClient() {
        SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE);
        pDialog.setTitleText("Вы хотите позвонить?")
                .setContentText(mClient.phone)
                .setConfirmText("Позвонить")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + mClient.phone));
                        startActivity(callIntent);
                        sDialog.dismissWithAnimation();
                    }
                })
                .setCancelText("Отмена")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                })
                .show();
    }

    private void cancelOrder() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alertdialog_decline_order);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        EditText reason = (EditText) dialog.findViewById(R.id.editTextDeclineReason);
        Button btnOkDialog = (Button) dialog.findViewById(R.id.buttonOkDecline);
        Button btnCancelDialog = (Button) dialog.findViewById(R.id.buttonCancelDecline);
        // if button is clicked, close the custom dialog
        btnOkDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order.status = OStatus.NEW;
                SendPostRequest(OStatus.NEW);
                showProgress(true);
                dialog.dismiss();
            }
        });

        btnCancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
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

    private void SendPostRequest(OStatus status) {
        if (mTask != null) {
            return;
        }

        mTask = new SendPostRequestTask(status, mClient.id);
        mTask.execute((Void) null);
    }

    private class SendPostRequestTask extends AsyncTask<Void, Void, JSONObject> {
        private String mDriver;
        private String mStatus;
        private String mCurrPosition;
        private String mId;

        SendPostRequestTask(OStatus type, int orderId) {
            mStatus = type.toString();
            mDriver = type == OStatus.NEW ? null : String.valueOf(user.id);
            mCurrPosition = gp.getPosition();
            mId = orderId == 0 ? null : String.valueOf(orderId);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject res = new JSONObject();
            JSONObject data = new JSONObject();
            try {
                data.put("status", mStatus);
                data.put("driver", mDriver == null ? JSONObject.NULL : mDriver);
                data.put("address_stop", mDriver == null ? JSONObject.NULL : gp.getPosition());
                if (mCurrPosition != null) data.put("address_stop", mCurrPosition);

                JSONObject object = api.getOrderRequest(null, "orders/" + mId + "/");
                if (mClient.status.equals(OStatus.NEW.toString())) {
                    if (Helper.isSuccess(object) && !object.getString("status").equals(OStatus.NEW.toString())) {
                        res = new JSONObject();
                        res.put("status", "reserved");
                    } else {
                        res = api.patchRequest(data, "orders/" + mId + "/");
                    }
                } else if ((mClient.status.equals(OStatus.ACCEPTED.toString())
                        || mClient.status.equals(OStatus.WAITING.toString())
                        || mClient.status.equals(OStatus.ONTHEWAY.toString()))
                        && !mStatus.equals(OStatus.NEW.toString())) {
                    if (Helper.isSuccess(object)) {
                        if (object.getString("status").equals(OStatus.CANCELED.toString())
                                || object.getInt("driver") != mClient.driver) {
                            res = new JSONObject();
                            res.put("status", "reserved");
                        }
                    }
                } else {
                    res = api.patchRequest(data, "orders/" + mId + "/");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                res = null;
            }
            return res;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            mTask = null;
            showProgress(false);
            try {
                if (Helper.isSuccess(result)) {
                    Toast.makeText(getActivity(), "Заказ обновлён", Toast.LENGTH_LONG).show();
                    if (result.getString("status").equals(OStatus.CANCELED.toString()))
                        order.clear();
                    else if (result.getString("status").equals(OStatus.ACCEPTED.toString())) {
                        mClient.status = OStatus.ACCEPTED.toString();
                        mClient.driver = user.id;
                        order.status = OStatus.ACCEPTED;
                        Helper.setOrder(result);
                    } else if (result.getString("status").equals(OStatus.NEW.toString())) {
                        order.clear();
                        Intent intent = new Intent();
                        intent.putExtra("returnCode", false);
                        getActivity().setResult(isActive ? 3 : 1, intent);
                        getActivity().finish();
                    }
                    updateViews();
                } else if (result != null && result.getString("status").equals("reserved")) {
                    Toast.makeText(getActivity().getApplicationContext(), "Заказ отменён или занят", Toast.LENGTH_SHORT).show();
                    order.clear();
                    getActivity().finish();
                }
                if (order.status == OStatus.ONTHEWAY) {
                    Intent intent = new Intent();
                    intent.putExtra("returnCode", true);
                    if (!isActive)
                        getActivity().setResult(1, intent);
                    else
                        getActivity().setResult(3, intent);
                    getActivity().finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {

            }
            updateViews();
        }

        @Override
        protected void onCancelled() {
            mTask = null;
        }
    }
}