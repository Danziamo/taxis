package taxi.city.citytaxidriver.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import taxi.city.citytaxidriver.R;
import taxi.city.citytaxidriver.models.GlobalSingleton;
import taxi.city.citytaxidriver.models.User;
import taxi.city.citytaxidriver.networking.RestClient;

public class AccountFragment extends Fragment {

    TextView tvAccountNumber;
    TextView tvAccountBalance;
    TextView tvRating;
    RatingBar ratingBar;
    User user;

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    public AccountFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account, container, false);
        user = GlobalSingleton.getInstance(getActivity()).currentUser;

        tvAccountNumber = (TextView)rootView.findViewById(R.id.textViewAccountNumber);
        tvAccountBalance = (TextView) rootView.findViewById(R.id.textViewAccountBalance);
        tvRating = (TextView) rootView.findViewById(R.id.textViewRating);
        ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);

        updateLabels();
        fetchTask();
        return rootView;
    }

    private String getRatingText(float rating) {
        if (rating < 1 ) return null;
        if (rating == 1) return "1 звезда";
        if (rating == 5) return "5 звезд";
        return String.valueOf(rating) + " звезды";
    }

    private void fetchTask(){
        RestClient.getUserService().getById(user.getId(), new Callback<User>() {
            @Override
            public void success(User mUser, Response response) {
                user.setBalance(mUser.getBalance());
                user.setRating(mUser.getRating());
                updateLabels();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void updateLabels() {
        tvAccountNumber.setText(user.getPhone());
        tvAccountBalance.setText(String.valueOf((int) user.getBalance()) + "  сом");
        tvRating.setText(getRatingText(user.getRating().getRating()));
        ratingBar.setRating(user.getRating().getRating());
    }
}