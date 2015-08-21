package taxi.city.citytaxidriver.nerworking;

import android.content.Context;

import retrofit.RequestInterceptor;
import taxi.city.citytaxiclient.App;
import taxi.city.citytaxiclient.models.GlobalSingleton;

public class SessionRequestInterceptor implements RequestInterceptor {
    Context context;

    public SessionRequestInterceptor() {
        this.context = App.getContext();
    }

    public SessionRequestInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public void intercept(RequestFacade request) {
        String token = GlobalSingleton.getInstance(context).token;
        if (token == null || token.isEmpty()) return;
        request.addHeader("Authorization", "Token " + token);
    }
}
