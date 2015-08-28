package taxi.city.citytaxidriver.fragments;

import android.support.v4.app.Fragment;

import taxi.city.citytaxidriver.BaseActivity;
import taxi.city.citytaxidriver.interfaces.ConfirmCallback;

public class BaseFragment extends Fragment {
    public void showProgress(String msg){
        BaseActivity activity = (BaseActivity) getActivity();
        activity.showProgress(msg);
    }

    public void hideProgress(){
        BaseActivity activity = (BaseActivity) getActivity();
        activity.hideProgress();
    }

    public void showConfirmDialog(String titleText, String confirmText, String cancelText, final ConfirmCallback callback){
        BaseActivity activity = (BaseActivity) getActivity();
        activity.showConfirmDialog(titleText, confirmText, cancelText, callback);
    }

    public void setActionBarTitle(String title) {
        BaseActivity activity = (BaseActivity) getActivity();
        activity.setActionBarTitle(title);
    }
}
