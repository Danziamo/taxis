package taxi.city.citytaxidriver.tasks;

import android.os.AsyncTask;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import taxi.city.citytaxidriver.App;
import taxi.city.citytaxidriver.core.Order;
import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.enums.OStatus;
import taxi.city.citytaxidriver.service.ApiService;
import taxi.city.citytaxidriver.utils.Helper;

/**
 * Created by mbt on 7/23/15.
 */
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
            JSONObject object = api.loginRequest(json, "login/");
            if (object != null) {
                if(object.has("status_code")){
                    statusCode = object.getInt("status_code");
                }

                String detail = "";
                if (object.has("detail")){
                    detail = object.getString("detail");
                }

                if (detail.toLowerCase().contains("account")) {
                    statusCode = NOT_ACTIVATED_ACCOUNT_STATUS_CODE;
                }

                if (statusCode == HttpStatus.SC_OK) {
                    user.setUser(object);
                    ApiService.getInstance().setToken(user.getToken());
                    Helper.saveUserPreferences(App.getContext(), user);
                    int id = object.getInt("id");
                    boolean hasCar = false;
                    if (object.has("cars") && object.getJSONArray("cars").length() > 0){
                        hasCar = true;
                        statusCode = ACCOUNT_HAS_CAR_STATUS_CODE;
                    }

                    if (hasCar) {
                        Helper.getOrderPreferences(App.getContext(), id);
                        if (Order.getInstance().id != 0) {
                            JSONObject orderObject = api.getRequest("", "orders/" + Order.getInstance().id);
                            if (Helper.isSuccess(orderObject)) {
                                if (orderObject.getString("status").equals(OStatus.FINISHED.toString())) {
                                    Order.getInstance().clear();
                                    Helper.destroyOrderPreferences(App.getContext(), id);
                                }
                            }
                        } else {
                            JSONObject orderObject = null;
                            JSONObject orderResult = api.getArrayRequest("", "info_orders/?status=accepted&ordering=-id&limit=1&driver=" + String.valueOf(user.id));
                            if (Helper.isSuccess(orderResult) && orderResult.getJSONArray("result").length() > 0) {
                                orderObject = orderResult.getJSONArray("result").getJSONObject(0);
                            }
                            orderResult = api.getArrayRequest("", "info_orders/?status=waiting&ordering=-id&limit=1&driver=" + String.valueOf(user.id));
                            if (Helper.isSuccess(orderResult) && orderResult.getJSONArray("result").length() > 0) {
                                orderObject = orderResult.getJSONArray("result").getJSONObject(0);
                            }
                            orderResult = api.getArrayRequest("", "info_orders/?status=ontheway&ordering=-id&limit=1&driver=" + String.valueOf(user.id));
                            if (Helper.isSuccess(orderResult) && orderResult.getJSONArray("result").length() > 0) {
                                orderObject = orderResult.getJSONArray("result").getJSONObject(0);
                            }
                            orderResult = api.getArrayRequest("", "info_orders/?status=pending&ordering=-id&limit=1&driver=" + String.valueOf(user.id));
                            if (Helper.isSuccess(orderResult) && orderResult.getJSONArray("result").length() > 0) {
                                orderObject = orderResult.getJSONArray("result").getJSONObject(0);
                            }
                            orderResult = api.getArrayRequest("", "info_orders/?status=sos&ordering=-id&limit=1&driver=" + String.valueOf(user.id));
                            if (Helper.isSuccess(orderResult) && orderResult.getJSONArray("result").length() > 0) {
                                orderObject = orderResult.getJSONArray("result").getJSONObject(0);
                            }
                            if (orderObject != null) {
                                Helper.setOrderFromLogin(orderObject);
                                Helper.saveOrderPreferences(App.getContext(), Order.getInstance());
                            }
                        }
                    }

                    JSONObject regObject = new JSONObject();
                    regObject.put("online_status", "online");
                    regObject.put("role", "driver");
                    regObject.put("ios_token", JSONObject.NULL);
                    api.patchRequest(regObject, "users/" + id + "/");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return statusCode;
    }

}
