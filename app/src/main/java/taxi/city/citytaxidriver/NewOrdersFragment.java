package taxi.city.citytaxidriver;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import taxi.city.citytaxidriver.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewOrdersFragment extends Fragment {

    public NewOrdersFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_orders, container, false);
    }
}
