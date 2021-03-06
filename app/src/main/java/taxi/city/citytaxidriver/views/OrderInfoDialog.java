package taxi.city.citytaxidriver.views;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.db.models.OrderModel;

/**
 * Created by mbt on 8/28/15.
 */
public class OrderInfoDialog extends Dialog {
    private Context mContext;

    private boolean isShowCalled = false;

    private TextView tvAddress;
    private TextView tvClientPhone;
    private TextView tvDescription;
    private TextView tvCounter;
    private TextView tvFixedPrice;
    private TextView tvStopAddress;
    private LinearLayout llFixedPrice;
    private Button btnCancel;
    private Button btnSubmit;

    public OrderInfoDialog(Context context) {
        super(context, R.style.MyAlertDialogStyle);
        mContext = context;
        setTitle(getString(R.string.alert_info_order));
        setContentView(R.layout.dialog_order_info);

        tvAddress = (TextView)findViewById(R.id.tvAddress);
        tvClientPhone = (TextView)findViewById(R.id.tvPhone);
        tvDescription = (TextView)findViewById(R.id.tvDescription);
        tvFixedPrice = (TextView)findViewById(R.id.tvFixedPrice);
        tvStopAddress = (TextView)findViewById(R.id.tvStopAddress);
        llFixedPrice = (LinearLayout)findViewById(R.id.llFixedPrice);
        tvCounter = (TextView)findViewById(R.id.counterView);

        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnSubmit = (Button)findViewById(R.id.btnSubmit);
    }

    public void setOrder(OrderModel order, boolean hasActiveOrder,  View.OnClickListener listener){
        isShowCalled = true;
        tvAddress.setText(order.getStartName());
        tvClientPhone.setText(order.getClientPhone());
        String desc = order.getDescription();
        if(!desc.equals("")) {
            tvDescription.setVisibility(View.VISIBLE);
            tvDescription.setText(order.getDescription());
        }else{
            tvDescription.setVisibility(View.GONE);
        }
        if (order.isFixedPrice()) {
            tvCounter.setVisibility(View.GONE);
            tvFixedPrice.setText(String.valueOf((int)order.getFixedPrice()));
        } else {
            llFixedPrice.setVisibility(View.GONE);
        }
        String stopAddress = order.getStopName();
        if(stopAddress.equals("")){
            tvStopAddress.setVisibility(View.GONE);
        }else{
            tvStopAddress.setVisibility(View.VISIBLE);
            tvStopAddress.setText(order.getStopName());
        }

        btnCancel.setOnClickListener(listener);
        if(hasActiveOrder) {
            btnSubmit.setVisibility(View.GONE);
        }else{
            btnSubmit.setOnClickListener(listener);
            btnSubmit.setVisibility(View.VISIBLE);
        }
        if(isShowCalled) {
            show();
        }
    }

    private String getString(int id){
        return mContext.getString(id);
    }


    @Override
    public void dismiss() {
        super.dismiss();
        isShowCalled = false;
    }

    public boolean isShowCalled(){
        return isShowCalled;
    }

    public void setShowCalled(boolean state){
        this.isShowCalled = state;
    }
}
