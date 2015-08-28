package taxi.city.citytaxidriver.utils;

public class Constants {

    public static final int GPS_MAX_ACCURACY = 50;

    // in meter/second
    public static final int GPS_MAX_SPEED = 30; //108 km/h

    public static final double ORDER_SEARCH_RANGE = 15; // km

    public static final double FIXED_PRICE = 70;

    public static final int DEFAULT_BORT_TARIFF = 2;

    public static final String SENDER_ID = "363431602762";

    public static final int SOS_DURATION = 10 * 60;

    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public static final long DRAWER_CLOSE_DELAY_MS = 250;

    public final static String[] PHONE_PREFIXES = {"+996", "+7", "+998"};




    public static final int NEW_ORDERS_KEY = 1;
    public static final int FINISH_ORDER_KEY = 2;
}
