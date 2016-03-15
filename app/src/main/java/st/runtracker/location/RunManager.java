package st.runtracker.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import st.runtracker.database.RunDatabaseHelper;
import st.runtracker.model.Run;

/**
 * Created by tengsun on 3/10/16.
 */
public class RunManager {

    private static final String TAG = "RunManager";
    private static final String PREFS_FILE = "runs";
    private static final String PREF_CURR_RUN_ID = "RunManager.currRunId";
    public static final String ACTION_LOCATION = "st.runtracker.ACTION_LOCATION";

    private static RunManager runManager;
    private Context appContext;
    private LocationManager locationManager;
    private RunDatabaseHelper dbHelper;
    private SharedPreferences prefs;
    private long currRunId;

    private RunManager(Context appContext) {
        this.appContext = appContext;
        locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        dbHelper = new RunDatabaseHelper(appContext);
        prefs = appContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        currRunId = prefs.getLong(PREF_CURR_RUN_ID, -1);
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

    private void startLocationUpdates() {
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

    private void stopLocationUpdates() {
        PendingIntent pi = getLocationPendingIntent(false);
        if (pi != null) {
            locationManager.removeUpdates(pi);
            pi.cancel();
        }
    }

    public boolean isTrackingRun() {
        return getLocationPendingIntent(false) != null;
    }

    public boolean isTrackingRun(Run run) {
        return run != null && run.getId() == currRunId;
    }

    private void broadcastLocation(Location location) {
        Intent boradcastIntent = new Intent(ACTION_LOCATION);
        boradcastIntent.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        appContext.sendBroadcast(boradcastIntent);
    }

    // database run table operation

    public Run startNewRun() {
        Run run = insertRun();
        currRunId = run.getId();
        prefs.edit().putLong(PREF_CURR_RUN_ID, currRunId).commit();
        startLocationUpdates();
        return run;
    }

    public void startTrackingRun(Run run) {
        // keep the ID
        currRunId = run.getId();
        // store it in shared preferences
        prefs.edit().putLong(PREF_CURR_RUN_ID, currRunId).commit();
        // start location updates
        startLocationUpdates();
    }

    public void stopRun() {
        stopLocationUpdates();
        currRunId = -1;
        prefs.edit().remove(PREF_CURR_RUN_ID).commit();
    }

    public Run insertRun() {
        Run run = new Run();
        run.setId(dbHelper.insertRun(run));
        Log.d(TAG, "Insert new run: " + run.getId());
        return run;
    }

    public RunDatabaseHelper.RunCursor queryRuns() {
        return dbHelper.queryRuns();
    }

    public Run getRun(long id) {
        Run run = null;
        RunDatabaseHelper.RunCursor cursor = dbHelper.queryRun(id);
        cursor.moveToFirst();

        if (!cursor.isAfterLast()) {
            run = cursor.getRun();
        }
        cursor.close();
        return run;
    }

    // database location table operation

    public void insertLocation(Location loc) {
        if (currRunId != -1) {
            dbHelper.insertLocation(currRunId, loc);
            Log.d(TAG, "Insert new location for run: " + currRunId);
        } else {
            Log.e(TAG, "Location received but no tracking run.");
        }
    }

    public Location getLastLocationForRun(long runId) {
        Location loc = null;
        RunDatabaseHelper.LocationCursor cursor = dbHelper.queryLastLocationForRun(runId);
        cursor.moveToFirst();

        if (!cursor.isAfterLast()) {
            loc = cursor.getLocation();
        }
        cursor.close();
        return loc;
    }

    public RunDatabaseHelper.LocationCursor queryLocationsForRun(long runId) {
        return dbHelper.queryLocationsForRun(runId);
    }

}
