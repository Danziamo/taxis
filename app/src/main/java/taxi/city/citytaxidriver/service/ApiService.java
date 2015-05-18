package taxi.city.citytaxidriver.service;

import android.text.TextUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import taxi.city.citytaxidriver.requestMethods.HttpPatch;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
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

    public JSONObject loginRequest(JSONObject data, String apiUrl) {

        HttpClient httpclient = new DefaultHttpClient();
        JSONObject res;

        try {
            HttpPost request = new HttpPost(url + apiUrl);
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
            // Add your data
            request.addHeader("content-type", "application/json");

            StringEntity params = new StringEntity(data.toString(), HTTP.UTF_8);
            request.setEntity(params);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            res = parseData(response);

        } catch (ClientProtocolException e) {
            res = null;
            // TODO Auto-generated catch block
        } catch (IOException e) {
            res = null;
            // TODO Auto-generated catch block
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
            res = null;
        }
        return res;
    }

    public JSONObject logoutRequest(JSONObject data, String apiUrl) {
        HttpClient httpclient = new DefaultHttpClient();
        JSONObject res;

        try {
            HttpPost request = new HttpPost(url + apiUrl);
            // Add your data
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "Token " + this.token);

            StringEntity params = new StringEntity(data.toString());
            request.setEntity(params);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(request);

            res = parseData(response);

        } catch (ClientProtocolException e) {
            res = null;
            // TODO Auto-generated catch block
        } catch (IOException e) {
            res = null;
            // TODO Auto-generated catch block
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
            res = null;
        }
        return res;
    }

    public JSONArray hasCar(JSONObject params, String apiUrl) {
        HttpClient httpclient = new DefaultHttpClient();
        JSONArray json = new JSONArray();

        try {
            HttpGet request = new HttpGet(url + apiUrl);
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "Token " + this.token);

            HttpResponse response = httpclient.execute(request);
            json = parseDataArray(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    public JSONArray fetchCarBrand(String apiUrl) {
        HttpClient httpclient = new DefaultHttpClient();
        JSONArray json = new JSONArray();

        try {
            HttpGet request = new HttpGet(url + apiUrl);
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "Token " + this.token);

            HttpResponse response = httpclient.execute(request);
            json = parseDataArray(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    public JSONObject createCar(JSONObject data, String apiUrl) {
        HttpClient httpclient = new DefaultHttpClient();
        JSONObject json;

        try {
            HttpPost request = new HttpPost(url + apiUrl);
            // Add your data
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "Token " + this.token);

            StringEntity params = new StringEntity(data.toString(), HTTP.UTF_8);
            request.setEntity(params);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(request);
            json = parseData(response);

        } catch (IOException e) {
            json = null;
            // TODO Auto-generated catch block
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
            json = null;
        }
        return json;
    }

    public JSONObject signUpRequest(JSONObject data, String apiUrl) {
        HttpClient httpclient = new DefaultHttpClient();
        JSONObject json = new JSONObject();

        try {
            HttpPost request = new HttpPost(url + apiUrl);
            // Add your data
            request.addHeader("content-type", "application/json");

            StringEntity params = new StringEntity(data.toString(), HTTP.UTF_8);
            request.setEntity(params);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(request);
            json = parseData(response);
        } catch (IOException e) {
            json = null;
            // TODO Auto-generated catch block
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
            json = null;
        }
        return json;
    }

    public JSONObject patchRequest(JSONObject data, String apiUrl) {
        HttpClient httpClient = new DefaultHttpClient();
        JSONObject json;

        try {
            HttpPatch request = new HttpPatch(url + apiUrl);
            // Add your data
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "Token " + this.token);

            StringEntity params = new StringEntity(data.toString(), HTTP.UTF_8);
            request.setEntity(params);

            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(request);
            json = parseData(response);

        } catch (IOException e) {
            json = null;
            // TODO Auto-generated catch block
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
            json = null;
        }
        return json;
    }

    public JSONObject activateRequest(JSONObject data, String apiUrl) {
        HttpClient httpClient = new DefaultHttpClient();
        JSONObject json = new JSONObject();

        try {
            HttpPatch request = new HttpPatch(url + apiUrl);
            // Add your data
            request.addHeader("content-type", "application/json");

            JSONObject object = new JSONObject();
            object.put("activation_code", data.getString("activation_code"));
            StringEntity params = new StringEntity(object.toString(), HTTP.UTF_8);
            request.setEntity(params);

            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(request);
            json = parseData(response);

        } catch (IOException e) {
            json = null;
            // TODO Auto-generated catch block
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
            json = null;
        }
        return json;
    }

    public JSONObject getOrderRequest(String params, String apiUrl) {
        JSONObject result = new JSONObject();
        try {
            HttpClient httpclient = new DefaultHttpClient();
            params = TextUtils.isEmpty(params) ? "" : params;
            HttpGet request = new HttpGet(url + apiUrl + params);
            // Add your data
            //request.addHeader("content-type", "application/json");
            request.setHeader("Authorization", "Token " + this.token);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(request);
            result = parseData(response);
        } catch (ClientProtocolException e) {
            result = null;
            // TODO Auto-generated catch block
        } catch (IOException e) {
            result = null;
            // TODO Auto-generated catch block
        }
        return result;
    }

    public JSONObject getDataFromGetRequest(String params, String apiUrl) {
        JSONObject result = new JSONObject();
        try {
            HttpClient httpclient = new DefaultHttpClient();
            params = TextUtils.isEmpty(params) ? "" : params;
            HttpGet request = new HttpGet(url + apiUrl + params);
            // Add your data
            //request.addHeader("content-type", "application/json");
            request.setHeader("Authorization", "Token " + this.token);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            result.put("status_code", statusCode);

            JSONArray object = parseDataArray(response);
            result.put("result", object);
        } catch (ClientProtocolException e) {
            result = null;
            // TODO Auto-generated catch block
        } catch (IOException e) {
            result = null;
            // TODO Auto-generated catch block
        } catch (JSONException e) {
            result = null;
            e.printStackTrace();
        }
        return result;
    }

    public Map.Entry<Integer, JSONObject> putDataRequest(JSONObject data, String apiUrl) {
        HttpClient httpclient = new DefaultHttpClient();
        Map.Entry<Integer, JSONObject> map;

        try {
            HttpPut request = new HttpPut(url + apiUrl);
            // Add your data
            request.setHeader("content-type", "application/json");
            request.setHeader("Authorization", "Token " + this.token);

            StringEntity params = new StringEntity(data.toString(), HTTP.UTF_8);
            request.setEntity(params);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            map = new AbstractMap.SimpleEntry<>(statusCode, null);

        } catch (ClientProtocolException e) {
            return null;
            // TODO Auto-generated catch block
        } catch (IOException e) {
            return null;
            // TODO Auto-generated catch block
        }
        return map;
    }

    protected JSONObject parseData(HttpResponse response) {
        JSONObject result = new JSONObject();
        int statusCode = response.getStatusLine().getStatusCode();
        try {
            result.put("status_code", statusCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            result = new JSONObject(sb.toString());
            result.put("status_code", statusCode);
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

            result = new JSONArray(sb.toString());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
