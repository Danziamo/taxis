package taxi.city.citytaxidriver.TaskService;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import taxi.city.citytaxidriver.Core.Order;
import taxi.city.citytaxidriver.Core.User;
import taxi.city.citytaxidriver.Enums.OrderStatus;
import taxi.city.citytaxidriver.Service.ApiService;

/**
 * Created by Daniyar on 4/8/2015.
 */
public class OrderTask {
    private Order order = Order.getInstance();
    private ApiService api = ApiService.getInstance();
    private User user = User.getInstance();
    private SendPostRequestTask sendTask = null;
    private Context context = null;

    public void sendOrderRequest(OrderStatus.STATUS type, Context context) {
        if (sendTask != null) {
            return;
        }

        this.context = context;
        sendTask = new SendPostRequestTask(type);
        sendTask.execute((Void) null);
    }

    private class SendPostRequestTask extends AsyncTask<Void, Void, JSONObject> {
        SendPostRequestTask(OrderStatus.STATUS type) {
            order.status = type;
            order.driver = user.id;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.

            JSONObject data = new JSONObject();
            try {
                data.put("status", order.status);
                data.put("driver", order.driver);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return api.patchRequest(data, "orders/" + order.id + "/");
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            sendTask = null;
            try {
                if (result != null && result.getInt("status_code") == HttpStatus.SC_OK) {
                    Toast.makeText(context, "Заказ обновлён", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Ошибка при отправке на сервер", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, "Ошибка при отправке на сервер", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            sendTask = null;
        }
    }
}
