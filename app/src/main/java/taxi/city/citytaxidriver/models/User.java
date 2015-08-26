package taxi.city.citytaxidriver.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import taxi.city.citytaxidriver.db.models.OrderModel;

public class User implements Serializable {

    @Expose
    private String phone;
    @Expose
    private String password;

    @Expose
    private int id;

    @Expose
    @SerializedName("first_name")
    private String firstName;
    @Expose
    @SerializedName("last_name")
    private String lastName;

    @Expose
    @SerializedName("online_status")
    private OnlineStatus onlineStatus;

    private String dateOfBirth;

    @Expose
    private String avatar;

    @Expose
    private double balance;

    @Expose
    private String email;

    @Expose
    @SerializedName("android_token")
    private String androidToken;

    @Expose
    @SerializedName("ios_token")
    private String iosToken;

    @Expose
    private Rating rating;

    @Expose
    private ArrayList<Car> cars;

    @Expose
    @SerializedName("cur_position")
    private String curPosition;

    private double latitude;
    private double longitude;

    @Expose
    @SerializedName("is_order_active")
    private ArrayList<OrderModel> activeOrders;

    @Expose
    private String token;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public OnlineStatus getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(OnlineStatus onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getDateOfBirth() {
        if (dateOfBirth == null) return "";
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getAndroidToken() {
        return androidToken;
    }

    public void setAndroidToken(String androidToken) {
        this.androidToken = androidToken;
    }

    public String getIosToken() {
        return iosToken;
    }

    public void setIosToken(String iosToken) {
        this.iosToken = iosToken;
    }

    public Rating getRating() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    public ArrayList<Car> getCars() {
        return cars;
    }

    public void setCars(ArrayList<Car> cars) {
        this.cars = cars;
    }

    public String getCurPosition() {
        return curPosition;
    }

    public void setCurPosition(String curPosition) {
        this.curPosition = curPosition;
        String s = this.curPosition;
        String regexPattern = "\\d+\\.?\\d*";
        if (s == null || s.isEmpty())
            return;
        List<String> geo = new ArrayList<>();
        Matcher m = Pattern.compile(regexPattern).matcher(s);
        while(m.find()) {
            geo.add(m.group());
        }
        if (geo.size() != 2)
            return;
        this.latitude = Double.valueOf(geo.get(0).trim());
        this.longitude = Double.valueOf(geo.get(1).trim());
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean hasActiveOrder() {
        return this.activeOrders != null && this.activeOrders.size() > 0;
    }

    public Order getActiveOrder() {
        if (hasActiveOrder()) {
            for (int i = this.activeOrders.size() - 1; i >= 0; i -= 1) {
                OrderModel orderModel = activeOrders.get(i);
                if (orderModel.getDriverId() == 0) continue;
                if (orderModel.getDriverId() != this.id) continue;
                return new Order(orderModel);
            }
        }
        return null;
    }

    public LatLng getLatLng() {
        String s = this.curPosition;
        String regexPattern = "\\d+\\.?\\d*";
        if (s == null || s.equals("null"))
            return null;
        List<String> geo = new ArrayList<>();
        Matcher m = Pattern.compile(regexPattern).matcher(s);
        while(m.find()) {
            geo.add(m.group());
        }
        if (geo.size() != 2)
            return null;
        double latitude = Double.valueOf(geo.get(0).trim());
        double longitude = Double.valueOf(geo.get(1).trim());
        return new LatLng(latitude, longitude);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return this.lastName + " " + this.firstName;
    }
}
