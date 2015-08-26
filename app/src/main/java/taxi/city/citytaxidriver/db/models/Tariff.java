package taxi.city.citytaxidriver.db.models;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mbt on 8/25/15.
 */

@Table(name="tariffs")
public class Tariff extends Model implements Serializable {

    @Expose
    @SerializedName("id")
    @Column(name = "tariff_id")
    private int tariffId;

    @Expose
    @Column(name = "tariff_name")
    private String tariffName;

    @Expose
    @Column(name = "seat_in_car_price")
    private double startPrice;

    @Expose
    @Column(name = "kilometer_price")
    private double ratio;

    @Expose
    @Column(name = "waiting_between_point_price")
    private double waitingRatio;

    @Expose
    @Column(name = "waiting_to_order")
    private long waitingToOrder;

    @Expose
    @Column(name = "waiting_to_order_price")
    private double waitingToOrderPrice;

    @Expose
    @Column(name = "airport_price")
    private double airportPrice;

    @Expose
    @Column(name = "animal_price")
    private double animalPrice;

    @Expose
    @Column(name = "delivery_kilometer_price")
    private double deliveryKilometerPrice;

    @Expose
    @Column(name = "delivery_price")
    private double deliveryPrice;

    @Expose
    @Column(name = "drunk_taxi_price")
    private double drunkTaxiPrice;

    public int getTariffId() {
        return tariffId;
    }

    public void setTariffId(int tariffId) {
        this.tariffId = tariffId;
    }

    public String getTariffName() {
        return tariffName;
    }

    public void setTariffName(String tariffName) {
        this.tariffName = tariffName;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public double getWaitingRatio() {
        return waitingRatio;
    }

    public void setWaitingRatio(double waitingRatio) {
        this.waitingRatio = waitingRatio;
    }

    public long getWaitingToOrder() {
        return waitingToOrder;
    }

    public void setWaitingToOrder(long waitingToOrder) {
        this.waitingToOrder = waitingToOrder;
    }

    public double getWaitingToOrderPrice() {
        return waitingToOrderPrice;
    }

    public void setWaitingToOrderPrice(double waitingToOrderPrice) {
        this.waitingToOrderPrice = waitingToOrderPrice;
    }

    public double getAirportPrice() {
        return airportPrice;
    }

    public void setAirportPrice(double airportPrice) {
        this.airportPrice = airportPrice;
    }

    public double getAnimalPrice() {
        return animalPrice;
    }

    public void setAnimalPrice(double animalPrice) {
        this.animalPrice = animalPrice;
    }

    public double getDeliveryKilometerPrice() {
        return deliveryKilometerPrice;
    }

    public void setDeliveryKilometerPrice(double deliveryKilometerPrice) {
        this.deliveryKilometerPrice = deliveryKilometerPrice;
    }

    public double getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(double deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    public double getDrunkTaxiPrice() {
        return drunkTaxiPrice;
    }

    public void setDrunkTaxiPrice(double drunkTaxiPrice) {
        this.drunkTaxiPrice = drunkTaxiPrice;
    }

    public static List<Tariff> getAll(){
        return new Select()
                .from(Tariff.class)
                .execute();
    }

    public static Tariff getTariffById(int tariffId){
        return new Select()
                .from(Tariff.class)
                .where("tariff_id = ?", tariffId)
                .executeSingle();
    }

    public static boolean isTariffsUpToDate(){
        long lastUpdateTime = Setting.getLongValue(Setting.TARIFFS_LAST_UPDATE_TIME_NAME);
        return (lastUpdateTime > (System.currentTimeMillis() - 24*60*60*1000));
    }

    public static void upgradeTariffs(List<Tariff> newTariffs){
        List<Tariff> currentTariffs = getAll();
        ActiveAndroid.beginTransaction();
        try{
            int size = currentTariffs.size();
            Tariff tariff;
            for(int i = 0; i < size; i++){
                tariff = currentTariffs.get(i);
                tariff.delete();
            }

            size = newTariffs.size();
            for(int i = 0; i < size; i++){
                tariff = newTariffs.get(i);
                tariff.save();
            }
            Setting.saveValue(Setting.TARIFFS_LAST_UPDATE_TIME_NAME, System.currentTimeMillis());
            ActiveAndroid.setTransactionSuccessful();
        }finally {
            ActiveAndroid.endTransaction();
        }
    }

}
