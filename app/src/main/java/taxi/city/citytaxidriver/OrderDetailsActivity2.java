package taxi.city.citytaxidriver;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import taxi.city.citytaxidriver.Core.Client;
import taxi.city.citytaxidriver.Utils.Helper;


public class OrderDetailsActivity2 extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_activity2);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new OrderDetailsFragment())
                    .commit();
        }
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class OrderDetailsFragment extends Fragment {
        private Client mClient;

        public OrderDetailsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_order_details_activity2, container, false);
            mClient = (Client)getActivity().getIntent().getSerializableExtra("DATA");

            TextView tvAddressStart = (TextView) rootView.findViewById(R.id.textViewStartAddress);
            TextView tvClientPhone = (TextView) rootView.findViewById(R.id.textViewClientPhone);
            TextView tvAddressStop = (TextView) rootView.findViewById(R.id.textViewStopAddress);
            TextView tvDescription = (TextView) rootView.findViewById(R.id.textViewDescription);
            TextView tvFixedPrice = (TextView) rootView.findViewById(R.id.textViewFixedPrice);
            LinearLayout llFixedPrice = (LinearLayout) rootView.findViewById(R.id.linearLayoutFixedPrice);

            tvAddressStart.setText(mClient.addressStart);
            tvClientPhone.setText(mClient.phone);
            tvAddressStop.setText(mClient.addressEnd);
            tvDescription.setText(mClient.description);
            double fixedPrice = Helper.getDouble(mClient.fixedPrice);
            tvFixedPrice.setText((int)fixedPrice + " сом");
            if (fixedPrice < 50) llFixedPrice.setVisibility(View.GONE);

            return rootView;
        }
    }
}
