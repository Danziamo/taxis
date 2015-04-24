package taxi.city.citytaxidriver.Core;

/**
 * Created by Daniyar on 4/24/2015.
 */
public class CarEntity {
    public int id;
    public String name;

    public CarEntity(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
