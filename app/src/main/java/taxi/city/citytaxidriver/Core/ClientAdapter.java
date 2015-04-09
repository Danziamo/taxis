package taxi.city.citytaxidriver.Core;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import taxi.city.citytaxidriver.R;

/**
 * Created by Daniyar on 3/18/2015.
 */
public class ClientAdapter extends ArrayAdapter<Client> {

    private static class ViewHolder {
        TextView id;
        TextView address;
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
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.id.setText(String.valueOf(client.id));
        viewHolder.address.setText(client.addressStart);
        return convertView;
    }
}