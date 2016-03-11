package st.runtracker.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

/**
 * Created by tengsun on 3/10/16.
 */
public class RunManager {

    private static final String TAG = "RunManager";
    public static final String ACTION_LOCATION = "st.runtracker.ACTION_LOCATION";

    private static RunManager runManager;
    private Context appContext;
    private LocationManager locationManager;

    private RunManager(Context appContext) {
        this.appContext = appContext;
        locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public static RunManager getRunManager(Context context) {
        if (runManager == null) {
            runManager = new RunManager(context.getApplicationContext());
        }
        return runManager;
    }

    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        Intent broadcastIntent = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(appContext, 0, broadcastIntent, flags);
    }

    public void startLocationUpdates() {
        String provider = LocationManager.GPS_PROVIDER;
        PendingIntent pi = getLocationPendingIntent(true);
        locationManager.requestLocationUpdates(provider, 0, 0, pi);
    }

    public void stopLocationUpdates() {
        PendingIntent pi = getLocationPendingIntent(false);
        if (pi != null) {
            locationManager.removeUpdates(pi);
            pi.cancel();
        }
    }

    public boolean isTrackingRun() {
        return getLocationPendingIntent(false) != null;
    }

}
