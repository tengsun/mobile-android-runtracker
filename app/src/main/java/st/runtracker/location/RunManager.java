package st.runtracker.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

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

        // get last known location and boradcast it
        Location lastKnown = locationManager.getLastKnownLocation(provider);
        if (lastKnown != null) {
            lastKnown.setTime(System.currentTimeMillis());
            broadcastLocation(lastKnown);
        }

        // check permission
        PendingIntent pi = getLocationPendingIntent(true);
        PackageManager pm = appContext.getPackageManager();
        boolean hasPermission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.ACCESS_FINE_LOCATION", "st.runtracker"));
        if (hasPermission) {
            locationManager.requestLocationUpdates(provider, 1000, 0, pi);
        } else {
            Log.i(TAG, "Location access is denied!");
        }
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


    private void broadcastLocation(Location location) {
        Intent boradcastIntent = new Intent(ACTION_LOCATION);
        boradcastIntent.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        appContext.sendBroadcast(boradcastIntent);
    }

}
