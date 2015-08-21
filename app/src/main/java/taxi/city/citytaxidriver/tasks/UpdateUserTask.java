package taxi.city.citytaxidriver.tasks;

import android.os.AsyncTask;

import org.json.JSONObject;

import taxi.city.citytaxidriver.core.User;
import taxi.city.citytaxidriver.networking.ApiService;

/**
 * Created by mbt on 8/4/15.
 */
public abstract class UpdateUserTask  extends AsyncTask<Void, Void, JSONObject> {

    private JSONObject json;
    private boolean isNew = false;
    public UpdateUserTask(JSONObject json) {
        this.json = json;
    }

    public UpdateUserTask(JSONObject json, boolean isNew) {
        this.json = json;
        this.isNew = isNew;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        User user = User.getInstance();
        if (isNew){
            return ApiService.getInstance().signUpRequest(json, "users/");
        }
        return ApiService.getInstance().patchRequest(json, "users/" + user.id + "/");
    }
}
