package taxi.city.citytaxidriver;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;
import taxi.city.citytaxidriver.interfaces.ConfirmCallback;

/**
 * Created by mbt on 8/24/15.
 */
public class BaseActivity extends AppCompatActivity {


    private SweetAlertDialog pDialog;


    public void showProgress(String msg){
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper()
                .setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(msg);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    public void hideProgress(){
        if(pDialog != null){
            pDialog.dismissWithAnimation();
        }
    }

    public void showConfirmDialog(String titleText, String confirmText, String cancelText, final ConfirmCallback callback){
        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        pDialog .setTitleText(titleText)
                .setConfirmText(confirmText)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        callback.confirm();
                        sDialog.dismissWithAnimation();

                    }
                })
                .setCancelText(cancelText)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        callback.cancel();
                        sDialog.dismissWithAnimation();
                    }
                })
                .show();
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

}
