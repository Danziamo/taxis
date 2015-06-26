package taxi.city.citytaxidriver.core;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.utils.Helper;

/**
 * Created by Daniyar on 3/18/2015.
 */
public class ClientAdapter extends ArrayAdapter<Client> {

    private static class ViewHolder {
        TextView id;
        TextView address;
        TextView distance;
        TextView fixedPrice;
    }

    public ClientAdapter(Context context, ArrayList<Client> clients) {
        super(context, R.layout.activity_order_list_item, clients);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Client client = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.activity_order_list_item, parent, false);
            viewHolder.id = (TextView) convertView.findViewById(R.id.orderId);
            viewHolder.address = (TextView) convertView.findViewById(R.id.orderAddress);
            viewHolder.distance = (TextView) convertView.findViewById(R.id.textViewOrderDistanceTo);
            viewHolder.fixedPrice = (TextView) convertView.findViewById(R.id.orderFixedPrice);

            switch (client.status) {
                case "finished":
                case "canceled":
                    viewHolder.id.setTextColor(Color.RED);
                    break;
                case "new":
                    viewHolder.id.setTextColor(Color.GREEN);
                    break;
                default:
                    viewHolder.id.setTextColor(Color.rgb(160,32,240));
                    break;
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        double mFixedPrice = 0;
        try {
            mFixedPrice = Double.valueOf(client.fixedPrice);
        } catch (Exception e) {
            mFixedPrice = 0;
        }

        float weightSum = 6;
        viewHolder.id.setText(String.valueOf(client.id));
        viewHolder.address.setText("#" + String.valueOf(client.id) + " " + client.addressStart);
        if (client.clientId == null) {
            viewHolder.address.setTextColor(getContext().getResources().getColor(R.color.blue_text));
        } else {
            viewHolder.address.setTextColor(getContext().getResources().getColor(R.color.blacktext2));
        }

        double distance = 0;
        LatLng driverPosition = GlobalParameters.getInstance().currPosition;
        LatLng clientPosition = Helper.getLatLng(client.startPoint);
        if (driverPosition != null && clientPosition != null && client.active) {
            Location driverLocation = new Location("");
            driverLocation.setLatitude(driverPosition.latitude);
            driverLocation.setLongitude(driverPosition.longitude);

            Location clientLocation = new Location("");
            clientLocation.setLatitude(clientPosition.latitude);
            clientLocation.setLongitude(clientPosition.longitude);
            viewHolder.distance.setText(Helper.getFormattedDistance(driverLocation.distanceTo(clientLocation)/1000) + "км");
        } else {
            viewHolder.distance.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0f));
            viewHolder.distance.setVisibility(View.GONE);
            weightSum += 2;
            viewHolder.address.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weightSum));
        }

        if (mFixedPrice >= 50 && client.active) {
            viewHolder.fixedPrice.setText(String.valueOf((int) mFixedPrice) + "сом");
            viewHolder.fixedPrice.setTextColor(this.getContext().getResources().getColor(R.color.red));
        } else {
            viewHolder.fixedPrice.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0f));
            viewHolder.fixedPrice.setVisibility(View.GONE);
            weightSum += 2;
            viewHolder.address.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weightSum));
        }
        return convertView;
    }
}