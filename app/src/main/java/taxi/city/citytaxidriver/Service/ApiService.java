package taxi.city.citytaxidriver.Service;

import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by Daniyar on 3/26/2015.
 */
public class ApiService {
    private static final String url = "http://81.88.192.37/api/v1/";
    private String token;

    private static ApiService mInstance = null;
    private ApiService() {
        this.token = "";
    }

    public static ApiService getInstance() {
        if (mInstance == null) {
            mInstance = new ApiService();
        }
        return mInstance;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    public Map.Entry<Integer,JSONObject> loginRequest(JSONObject data, String apiUrl) {

        HttpClient httpclient = new DefaultHttpClient();

        try {
            HttpPost request = new HttpPost(url + apiUrl);
            // Add your data
            request.addHeader("content-type", "application/json");

            StringEntity params = new StringEntity(data.toString());
            request.setEntity(params);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            JSONObject object = parseData(response);
            return new AbstractMap.SimpleEntry<>(statusCode, object);

        } catch (ClientProtocolException e) {
            return null;
            // TODO Auto-generated catch block
        } catch (IOException e) {
            return null;
            // TODO Auto-generated catch block
        }
    }

    public Map.Entry<Integer,JSONObject> getDataFromPostRequest(JSONObject data, String apiUrl) {

        HttpClient httpclient = new DefaultHttpClient();

        try {
            HttpPost request = new HttpPost(url + apiUrl);
            // Add your data
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "Token " + this.token);

            StringEntity params = new StringEntity(data.toString());
            request.setEntity(params);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            JSONObject object = parseData(response);
            return new AbstractMap.SimpleEntry<>(statusCode, object);

        } catch (ClientProtocolException e) {
            return null;
            // TODO Auto-generated catch block
        } catch (IOException e) {
            return null;
            // TODO Auto-generated catch block
        }
    }

    public Map.Entry<Integer, JSONArray> getDataFromGetRequest(String params, String apiUrl) {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet request = new HttpGet(url + apiUrl);//+ "?" + params);
            // Add your data
            //request.addHeader("content-type", "application/json");
            request.setHeader("Authorization", "Token " + this.token);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            JSONArray object = parseDataArray(response);
            return new AbstractMap.SimpleEntry<>(statusCode, object);

        } catch (ClientProtocolException e) {
            return null;
            // TODO Auto-generated catch block
        } catch (IOException e) {
            return null;
            // TODO Auto-generated catch block
        } /*catch (Exception e) {
            Log.e("ApiError", e.getStackTrace().toString());
            return null;
        }*/
    }

    public Map.Entry<Integer, JSONObject> putDataRequest(JSONObject data, String apiUrl) {
        HttpClient httpclient = new DefaultHttpClient();

        try {
            HttpPut request = new HttpPut(url + apiUrl);
            // Add your data
            request.setHeader("content-type", "application/json");
            request.setHeader("Authorization", "Token " + this.token);

            StringEntity params = new StringEntity(data.toString());
            request.setEntity(params);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.d("API", getResponseMessage(response));
            }
            return new AbstractMap.SimpleEntry<>(statusCode, null);

        } catch (ClientProtocolException e) {
            return null;
            // TODO Auto-generated catch block
        } catch (IOException e) {
            Log.e("API", Arrays.toString(e.getStackTrace()));
            return null;
            // TODO Auto-generated catch block
        }
    }

    protected JSONObject parseData(HttpResponse response) {
        JSONObject result = null;
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            result = new JSONObject(sb.toString());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    protected String getResponseMessage(HttpResponse response) {
        try {
            BufferedReader rd = null;
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            String res = sb.toString();
            return  res;
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    protected JSONArray parseDataArray(HttpResponse response) {
        JSONArray result = null;
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            Log.d("Response", sb.toString());
            result = new JSONArray(sb.toString());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
