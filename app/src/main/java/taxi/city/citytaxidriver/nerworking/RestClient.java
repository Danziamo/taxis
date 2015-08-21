package taxi.city.citytaxidriver.nerworking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import taxi.city.citytaxiclient.App;
import taxi.city.citytaxiclient.R;
import taxi.city.citytaxiclient.networking.api.OrderApi;
import taxi.city.citytaxiclient.networking.api.SessionApi;
import taxi.city.citytaxiclient.networking.api.UserApi;

public class RestClient {
    private RestClient() {
    }

    private static RestAdapter getRestAdapter() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(App.getContext().getResources().getString(R.string.api_url_path))
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new SessionRequestInterceptor())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(new OkClient(new OkHttpClient()));

        return builder.build();
    }

    public static <S> S createService(Class<S> serviceClass) {
        RestAdapter adapter = getRestAdapter();
        return adapter.create(serviceClass);
    }

    public static UserApi getUserService() {
        return createService(UserApi.class);
    }

    public static OrderApi getOrderService() {
        return createService(OrderApi.class);
    }

    public static SessionApi getSessionService() {
        return createService(SessionApi.class);
    }
}
