package taxi.city.citytaxidriver.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Rating implements Serializable{

    @Expose
    @SerializedName("votes__sum")
    private int sum;

    @Expose
    @SerializedName("votes__count")
    private int count;

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public float getRating() {
        if (count == 0) return 0;
        return (float)sum/count;
    }
}
