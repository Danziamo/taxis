package taxi.city.citytaxidriver.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import taxi.city.citytaxidriver.App;
import taxi.city.citytaxidriver.core.Order;
import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.enums.OStatus;
import taxi.city.citytaxidriver.service.ApiService;
import taxi.city.citytaxidriver.utils.Helper;

public abstract class UserLoginTask extends AsyncTask<Void, Void, Integer> {

    private final String mPhone;
    private final String mPassword;

    public static final int NOT_ACTIVATED_ACCOUNT_STATUS_CODE = 1000;
    public static final int ACCOUNT_HAS_CAR_STATUS_CODE = 1001;

    public UserLoginTask(String phone, String password) {
        mPhone = phone;
        mPassword = password;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        User user = User.getInstance();
        ApiService api = ApiService.getInstance();

        int statusCode = 500;
        try {

            JSONObject json = new JSONObject();
            user.phone = mPhone;
            user.password = mPassword;
            json.put("phone", mPhone);
            json.put("password", mPassword);
            JSONObject loginResult = api.loginRequest(json, "login/");
            if (loginResult != null) {
                if(loginResult.has("status_code")){
                    statusCode = loginResult.getInt("status_code");
                }

                String detail = "";
                if (loginResult.has("detail")){
                    detail = loginResult.getString("detail");
                }

                if (detail.toLowerCase().contains("account")) {
                    statusCode = NOT_ACTIVATED_ACCOUNT_STATUS_CODE;
                }

                if (statusCode == HttpStatus.SC_OK) {
                    user.setUser(loginResult);
                    ApiService.getInstance().setToken(user.getToken());
                    Helper.saveUserPreferences(App.getContext(), user);
                    int id = loginResult.getInt("id");
                    boolean hasCar = false;
                    if (loginResult.has("cars") && loginResult.getJSONArray("cars").length() > 0){
                        hasCar = true;
                        statusCode = ACCOUNT_HAS_CAR_STATUS_CODE;
                    }

                    if (hasCar) {
                        Helper.getOrderPreferences(App.getContext(), id);
                        if (Order.getInstance().id != 0
                                && (Order.getInstance().status == OStatus.CANCELED)
                                || Order.getInstance().status == OStatus.NEW) {
                            Helper.destroyOrderPreferences(App.getContext(), id);
                            Order.getInstance().clear();
                        }
                        if (Order.getInstance().id != 0) {
                            JSONObject orderObject = api.getRequest("", "orders/" + Order.getInstance().id);
                            if (Helper.isSuccess(orderObject) || Helper.isBadRequest(orderObject)) {
                                if (orderObject.getString("status").equals(OStatus.FINISHED.toString())
                                        || orderObject.getString("status").equals(OStatus.CANCELED.toString())
                                        || orderObject.getString("status").equals(OStatus.NEW.toString())) {
                                    Order.getInstance().clear();
                                    Helper.destroyOrderPreferences(App.getContext(), id);
                                }
                            }
                        } else if (loginResult.has("is_order_active") && loginResult.getJSONArray("is_order_active").length() > 0) {
                            JSONArray jsonArray = loginResult.getJSONArray("is_order_active");
                            for (int i = 0; i < jsonArray.length(); ++i) {
                                JSONObject orderObject = jsonArray.getJSONObject(i);
                                if (orderObject.has("status") && orderObject.getString("status").equals(OStatus.NEW.toString())) continue;
                                if (orderObject.has("driver") && orderObject.getString("driver").equals("null")) continue;
                                if (orderObject.has("driver") && orderObject.getInt("driver") != user.id ) continue;
                                Helper.setOrderFromLogin(orderObject);
                                Helper.saveOrderPreferences(App.getContext(), Order.getInstance());
                                break;
                            }
                        }
                    }

                    JSONObject regObject = new JSONObject();
                    regObject.put("role", "driver");
                    regObject.put("ios_token", JSONObject.NULL);
                    regObject.put("online_status", "online");
                    api.patchRequest(regObject, "users/" + id + "/");
                }
            }
        } catch (JSONException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
        return statusCode;
    }

}
