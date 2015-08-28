package taxi.city.citytaxidriver.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import taxi.city.citytaxidriver.NewOrdersActivity;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.db.models.OrderModel;
import taxi.city.citytaxidriver.models.Order;

public class NewOrderAdapter extends RecyclerView.Adapter<NewOrderAdapter.ViewHolder> {
    private ArrayList<OrderModel> items;
    private int itemLayout;
    private final Context mContext;

    public NewOrderAdapter(ArrayList<OrderModel> items, int layout, Context context) {
        this.items = items;
        this.itemLayout = layout;
        this.mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mInfoView;
        public TextView mAddressView;
        public TextView mPriceView;
        public TextView mDistanceView;

        public ViewHolder(final View v) {
            super(v);
            mInfoView = (TextView) itemView.findViewById(R.id.tvInfoText);
            mAddressView = (TextView) itemView.findViewById(R.id.tvAddress);
            mDistanceView = (TextView) itemView.findViewById(R.id.tvDistance);
            mPriceView = (TextView) itemView.findViewById(R.id.tvPrice);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OrderModel order = (OrderModel)v.getTag();
                    Intent intent = new Intent();
                    intent.putExtra("DATA", order);
                    ((NewOrdersActivity)mContext).setResult(Activity.RESULT_OK, intent);
                    ((NewOrdersActivity)mContext).finish();
                }
            });
        }
    }

    @Override
    public NewOrderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        OrderModel item = items.get(position);
        holder.mAddressView.setText(item.getStartName());
        holder.mInfoView.setText(item.getDescription());
        if(item.isFixedPrice()) {
            holder.mPriceView.setText(String.valueOf((int) item.getTotalSum()) + mContext.getResources().getString(R.string.meter_currency));
        }else{
            holder.mPriceView.setText("");
        }
        holder.mDistanceView.setText(String.valueOf((int) item.getDistance()) + mContext.getResources().getString(R.string.meter_distance));
        holder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setDataset(ArrayList<OrderModel> dataset) {
        items = dataset;
        // This isn't working
        notifyItemRangeInserted(0, items.size());
        notifyDataSetChanged();
    }
}