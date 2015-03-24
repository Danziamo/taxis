package taxi.city.citytaxidriver.Core;

/**
 * Created by Daniyar on 3/23/2015.
 */
public class Trip {

    private double freeDistance = 0;
    private double chargePrice = 0;
    private double ratio = 1;

    private double distance = 0;
    private double time = 0;
    private double pauseTime = 0;
    private double pauseFeeRatio = 1;
    private double pauseFreeTime = 0;

    public void Trip (double distance, double freeDistance, double chargePrice, double ratio, double pauseFeeRatio, double pauseFreeTime) {
        this.distance = distance;
        this.freeDistance = freeDistance;
        this.chargePrice = chargePrice;
        this.ratio = ratio;
    }

    public void setDistance (double distance) {
        this.distance = distance;
    }

    public double getDistance () {
        return this.distance;
    }
    public double getPrice() {
        return chargePrice + (distance - freeDistance)*ratio;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getTime() {
        return this.time;
    }
}
