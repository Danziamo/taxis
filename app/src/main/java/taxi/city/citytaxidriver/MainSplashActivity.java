package taxi.city.citytaxidriver;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.maps.MapFragment;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import taxi.city.citytaxiclient.fragments.LoginFragment;
import taxi.city.citytaxiclient.models.GlobalSingleton;
import taxi.city.citytaxiclient.models.OnlineStatus;
import taxi.city.citytaxiclient.models.Role;
import taxi.city.citytaxiclient.models.Session;
import taxi.city.citytaxiclient.networking.RestClient;
import taxi.city.citytaxiclient.networking.model.UserStatus;
import taxi.city.citytaxiclient.service.ApiService;
import taxi.city.citytaxiclient.utils.SessionHelper;
import taxi.city.citytaxidriver.fragments.MapsFragment;

public class MainSplashActivity extends BaseActivity implements View.OnClickListener {

    View animContainer;
    View bottomMiniPanel;
    private YoYo.YoYoString animation;

    private static int SPLASH_TIME_OUT = 1000;
    private static int MIN_TIME_OUT    = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_splash);

        animContainer   = findViewById(R.id.main_navigation_panel);
        bottomMiniPanel = findViewById(R.id.anim_panel);

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {

            @Override
            public void onBackStackChanged() {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.container);
                if (f != null){
                    updateTitleAndDrawer (f);
                }

            }
        });

        final SessionHelper sessionHelper = new SessionHelper();
        String phone = sessionHelper.getPhone();
        String password = sessionHelper.getPassword();

        if(!phone.isEmpty() && !password.isEmpty()){
            performLogIn(phone,password);

        }else {

            TextView tvLogin = (TextView) findViewById(R.id.s_signin);
            TextView tvSignup = (TextView) findViewById(R.id.s_regist);

            tvLogin.setOnClickListener(this);
            tvSignup.setOnClickListener(this);

            openAnimation();
        }


    }

    private void saveSessionPreferencesNew(taxi.city.citytaxidriver.models.User user) {
        SessionHelper sessionHelper = new SessionHelper();
        sessionHelper.setPhone(user.getPhone());
        sessionHelper.setId(user.getId());
        sessionHelper.setToken(user.getToken());
        ApiService.getInstance().setToken(user.getToken());
    }

    private void openAnimation(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                performAnim(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        performAnim(false);
                    }
                }, SPLASH_TIME_OUT);

            }
        }, MIN_TIME_OUT);
    }

    public void performAnim(Boolean flag){
        if(flag)
            animContainer.setVisibility(View.INVISIBLE);
        else
            animContainer.setVisibility(View.VISIBLE);

        bottomMiniPanel.setVisibility(View.VISIBLE);
        animation = YoYo.with(Techniques.SlideInUp)
                .duration(800)
                .startPoint(0)
                .playOn(animContainer);
    }

    public void performLogIn(String phone,String password){
        Session session = new Session();
        session.setPhone(phone);
        session.setPassword(password);
        RestClient.getSessionService().login(session, new Callback<taxi.city.citytaxidriver.models.User>() {
            @Override
            public void success(taxi.city.citytaxidriver.models.User user, Response response) {
                GlobalSingleton.getInstance(MainSplashActivity.this).token = user.getToken();
                GlobalSingleton.getInstance(MainSplashActivity.this).currentUser = user;
                saveSessionPreferencesNew(user);

                if (user.hasActiveOrder() && user.getActiveOrder() != null) {
                    GlobalSingleton.getInstance(MainSplashActivity.this).currentOrder = user.getActiveOrder();
                }

                UserStatus userStatus = new UserStatus();
                userStatus.iosToken = null;
                userStatus.onlineStatus = OnlineStatus.ONLINE;
                userStatus.role = Role.USER;
                RestClient.getUserService().updateStatus(user.getId(), userStatus, new Callback<taxi.city.citytaxidriver.models.User>() {
                    @Override
                    public void success(taxi.city.citytaxidriver.models.User user, Response response) {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(MainSplashActivity.this, MapFragment.class);
                                startActivity(intent);
                                finish();
                            }
                        }, SPLASH_TIME_OUT);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(MainSplashActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainSplashActivity.this, MapsFragment.class);
                        startActivity(intent);
                        finish();
                        Crashlytics.logException(error);
                    }
                });
             //   Toast.makeText(MainSplashActivity.this, "Success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(RetrofitError error) {
                String message = "";
                if (error.getKind() == RetrofitError.Kind.HTTP) {
                    String json = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                    if (json.toLowerCase().contains("username or password")) {
                        message = "Телефон или пароль неверны";
                    } else if (json.toLowerCase().contains("account")) {
                        message = "Аккаунт не активирован";
                    }
                } else {
                    message = "Не удалось подключится к серверу";
                }
                Toast.makeText(MainSplashActivity.this, message, Toast.LENGTH_SHORT).show();

                openAnimation();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        Intent intent;

        switch (id){
            case R.id.s_signin:
                intent = new Intent(MainSplashActivity.this, LoginActivity.class);
                startActivity(intent);
                setTitle("Войти");
                break;

            case R.id.s_regist:
                intent = new Intent(MainSplashActivity.this, SignupActivity.class);
                startActivity(intent);
                setTitle("Регистрация");
                break;
        }
    }

    private void updateTitleAndDrawer (Fragment fragment){
        String fragClassName = fragment.getClass().getName();

        if (fragClassName.equals(LoginFragment.class.getName())){
            setTitle ("Войти");
            //set selected item position, etc
        }
        else if (fragClassName.equals(SignupActivity.class.getName())){
            setTitle ("Регистрация");
            //set selected item position, etc
        }
        /*else if (fragClassName.equals(C.class.getName())){
            setTitle ("C");
            //set selected item position, etc
        }*/
    }
}
