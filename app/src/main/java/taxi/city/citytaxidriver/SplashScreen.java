/**
 * Created by taxi on 13/05/15.
 */
package taxi.city.citytaxidriver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import org.apache.http.HttpStatus;

import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.tasks.UserLoginTask;
import taxi.city.citytaxidriver.utils.Helper;
import taxi.city.citytaxidriver.utils.SessionHelper;

public class SplashScreen extends Activity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final SessionHelper sessionHelper = new SessionHelper();
        String phone = sessionHelper.getPhone();
        String password = sessionHelper.getPassword();

        if(!phone.isEmpty() && !password.isEmpty()){
            goToLoginActivity();
            //@TODO after autologin upgrade tariffs: Helper.upgradeTariffInBackgroud();
            /*new UserLoginTask(phone, password){
                @Override
                protected void onPostExecute(final Integer statusCode) {
                    super.onPostExecute(statusCode);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(statusCode == HttpStatus.SC_OK || statusCode == UserLoginTask.ACCOUNT_HAS_CAR_STATUS_CODE){
                                User user = User.getInstance();
                                sessionHelper.save(user);
                                Helper.saveUserPreferences(SplashScreen.this, user);
                                boolean hasCar = (statusCode == UserLoginTask.ACCOUNT_HAS_CAR_STATUS_CODE);
                                goToNextActivity(hasCar);
                            }else if(statusCode == UserLoginTask.NOT_ACTIVATED_ACCOUNT_STATUS_CODE){
                                goToActivation();
                            }else{
                                goToLoginActivity();
                            }
                        }
                    }, SPLASH_TIME_OUT);
                }
            }.execute();*/
        }else{
            new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

                @Override
                public void run() {
                    goToLoginActivity();
                }
            }, SPLASH_TIME_OUT);
        }


    }

    private void goToActivation() {
        SessionHelper sessionHelper = new SessionHelper();
        Intent intent = new Intent(SplashScreen.this, ConfirmSignUpActivity.class);
        intent.putExtra(ConfirmSignUpActivity.PHONE_KEY, sessionHelper.getPhone());
        intent.putExtra(ConfirmSignUpActivity.PASSWORD_KEY, sessionHelper.getPassword());
        startActivity(intent);
        finish();
    }

    private void goToLoginActivity(){
        Intent i = new Intent(SplashScreen.this, LoginActivity.class);
        startActivity(i);

        finish();
    }

    private void goToNextActivity(boolean hasCar) {

        Intent intent;
        if (hasCar) {
            intent = new Intent(SplashScreen.this, MapsActivity.class);
        } else {
            intent = new Intent(SplashScreen.this, CarDetailsActivity.class);
        }
        intent.putExtra("NEW", true);
        startActivity(intent);
        finish();
    }

}
