package flex.com.sklepik;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

@SuppressWarnings("unused")
public class MapStateManager {
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String ZOOM = "zoom";
    private static final String BEARING = "bearing";
    private static final String TILT = "tilt";
    private static final String MAPTYPE = "MAPTYPE";

    private static final String PREFS_NAME = "mapCameraState";

    private SharedPreferences mapStatePrefs;

    public MapStateManager(Context context) {
        mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void zapiszStanMapy(GoogleMap map) {
        SharedPreferences.Editor edytor = mapStatePrefs.edit();
        CameraPosition pozycja = map.getCameraPosition();

        edytor.putFloat(LATITUDE, (float) pozycja.target.latitude);
        edytor.putFloat(LONGITUDE, (float) pozycja.target.longitude);
        edytor.putFloat(ZOOM, pozycja.zoom);
        edytor.putFloat(BEARING, pozycja.bearing);
        edytor.putFloat(TILT, pozycja.tilt);
        edytor.putInt(MAPTYPE, map.getMapType());

        edytor.commit();
    }

    public CameraPosition pobierzZapisanyStan() {
        double szer = mapStatePrefs.getFloat(LATITUDE, 0);
        if (szer == 0) {
            return null;
        }
        double dlug = mapStatePrefs.getFloat(LONGITUDE, 0);
        LatLng cel = new LatLng(szer, dlug);
        float zoom = mapStatePrefs.getFloat(ZOOM, 0);
        float bearing = mapStatePrefs.getFloat(BEARING, 0);
        float tilt = mapStatePrefs.getFloat(TILT, 0);

        CameraPosition pozycja = new CameraPosition(cel, zoom, tilt, bearing);
        return pozycja;
    }

    public int pobierzTypMapy() {
        int typ = mapStatePrefs.getInt(MAPTYPE, 0);
        return typ;
    }
}
