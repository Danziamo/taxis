package taxi.city.citytaxidriver.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import taxi.city.citytaxidriver.fragments.AccountFragment;
import taxi.city.citytaxidriver.fragments.CarDetailsFragment;
import taxi.city.citytaxidriver.fragments.HistoryFragment;
import taxi.city.citytaxidriver.fragments.MapsFragment;
import taxi.city.citytaxidriver.fragments.UserDetailsFragment;

public class TabsPagerAdapter extends FragmentStatePagerAdapter {

    private final String[] TITLES = { "Главная", "Счет", "История" };
    SparseArray<Fragment> registeredFragments = new SparseArray<>();

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
                return new MapsFragment();
            case 1:
                return new AccountFragment();
            case 2:
                return new HistoryFragment();
            default:
                return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
