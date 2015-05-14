package taxi.city.citytaxidriver;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import taxi.city.citytaxidriver.fragments.AccountFragment;
import taxi.city.citytaxidriver.fragments.CarDetailsFragment;
import taxi.city.citytaxidriver.fragments.FinishOrderDetailsFragment;
import taxi.city.citytaxidriver.fragments.OrderActivityFragment;
import taxi.city.citytaxidriver.fragments.UserDetailsFragment;

public class TabsPagerAdapter extends FragmentStatePagerAdapter {

    private final String[] TITLES = { "Счёт", "Личные", "Авто", "Транспорт" };
    private final int[] ICONS = {R.drawable.ic_action_account, R.drawable.ic_action_personal , R.drawable.ic_action_transport, R.drawable.ic_action_history};
    private final int[] SELECTED_ICONS = {R.drawable.ic_action_account_selected, R.drawable.ic_action_personal_selected, R.drawable.ic_action_history_selected, R.drawable.ic_action_transport_selected};

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return AccountFragment.newInstance();
            case 1:
                return UserDetailsFragment.newInstance();
            case 2:
                return CarDetailsFragment.newInstance();
            case 3:
                //return OrderActivityFragment.newInstance();
            default:
                return null;
        }

    }
}
