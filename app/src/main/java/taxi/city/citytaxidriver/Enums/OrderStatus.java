package taxi.city.citytaxidriver.Enums;

/**
 * Created by Daniyar on 3/30/2015.
 */
public class OrderStatus {
    public enum STATUS {
        NEW ("new"),
        ACCEPTED ("accepted"),
        SOS ("sos"),
        CANCELLED ("cancelled"),
        FINISHED ("finished");

        private final String name;

        private STATUS(String s) {
            name = s;
        }

        public boolean equalsName(String name) {
            return (name == null) ? false : this.name.equals(name);
        }

        public String toString() {
            return name;
        }
    }
}
