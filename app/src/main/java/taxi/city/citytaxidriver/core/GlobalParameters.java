package taxi.city.citytaxidriver.core;

import com.google.android.gms.maps.model.LatLng;

public class GlobalParameters {
    private static GlobalParameters ourInstance = new GlobalParameters();

    public LatLng currPosition;

    public static GlobalParameters getInstance() {
        if (ourInstance == null) ourInstance = new GlobalParameters();
        return ourInstance;
    }

    private GlobalParameters() {
    }

    public String getPosition() {
        if (currPosition == null) return null;
        return "POINT (" + currPosition.latitude + " " + currPosition.longitude + ")";
    }
}
