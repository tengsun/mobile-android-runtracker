package st.runtracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.util.Date;

import st.runtracker.model.Run;

/**
 * Created by tengsun on 3/11/16.
 */
public class RunDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "runs.sqlite";
    private static final int VERSION = 1;

    private static final String TABLE_RUN = "run";
    private static final String COLUMN_RUN_ID = "_id";
    private static final String COLUMN_RUN_START_DATE = "start_date";

    private static final String TABLE_LOCATION = "location";
    private static final String COLUMN_LOCATION_RUN_ID = "run_id";
    private static final String COLUMN_LOCATION_RUN_TIME = "run_time";
    private static final String COLUMN_LOCATION_LAT = "latitude";
    private static final String COLUMN_LOCATION_LNG = "longitude";
    private static final String COLUMN_LOCATION_ALT = "altitude";
    private static final String COLUMN_LOCATION_PROVIDER = "provider";

    public RunDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create "run" table
        db.execSQL("create table run "
                + "(_id integer primary key autoincrement, start_date integer)");
        // create "location" table
        db.execSQL("create table location "
                + "(run_id integer references run(_id), "
                + "run_time integer, latitude real, longitude real, altitude real, "
                + "provider varchar(100))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // implement schema changes and data message when upgrading
    }

    public long insertRun(Run run) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_RUN_START_DATE, run.getStartDate().getTime());
        return getWritableDatabase().insert(TABLE_RUN, null, cv);
    }

    public long insertLocation(long runId, Location location) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LOCATION_RUN_ID, runId);
        cv.put(COLUMN_LOCATION_RUN_TIME, location.getTime());
        cv.put(COLUMN_LOCATION_LAT, location.getLatitude());
        cv.put(COLUMN_LOCATION_LNG, location.getLongitude());
        cv.put(COLUMN_LOCATION_ALT, location.getAltitude());
        cv.put(COLUMN_LOCATION_PROVIDER, location.getProvider());
        return getWritableDatabase().insert(TABLE_LOCATION, null, cv);
    }

    public RunCursor queryRuns() {
        Cursor cursor = getReadableDatabase().query(TABLE_RUN,
                null, null, null, null, null, COLUMN_RUN_START_DATE + " asc");
        return new RunCursor(cursor);
    }

    public RunCursor queryRun(long id) {
        Cursor cursor = getReadableDatabase().query(TABLE_RUN,
                null, // all columns
                COLUMN_RUN_ID + " = ?", // look for a run id
                new String[]{String.valueOf(id)}, // with this value
                null, // group by
                null, // having
                null, // order by
                "1"); // limit 1 row
        return new RunCursor(cursor);
    }

    public LocationCursor queryLastLocationForRun(long runId) {
        Cursor cursor = getReadableDatabase().query(TABLE_LOCATION,
                null, // all columns
                COLUMN_LOCATION_RUN_ID + " = ?", // look for a run id
                new String[]{String.valueOf(runId)}, // with this value
                null, // group by
                null, // having
                COLUMN_LOCATION_RUN_TIME + " desc", // order by
                "1"); // limit 1 row
        return new LocationCursor(cursor);
    }

    public LocationCursor queryLocationsForRun(long runId) {
        Cursor cursor = getReadableDatabase().query(TABLE_LOCATION,
                null, // all columns
                COLUMN_LOCATION_RUN_ID + " = ?", // look for a run id
                new String[]{String.valueOf(runId)}, // with this value
                null, // group by
                null, // having
                COLUMN_LOCATION_RUN_TIME + " asc"); // order by
        return new LocationCursor(cursor);
    }

    // run and location cursor wrapper

    public static class RunCursor extends CursorWrapper {
        public RunCursor(Cursor cursor) {
            super(cursor);
        }

        public Run getRun() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }

            Run run = new Run();
            long runId = getLong(getColumnIndex(COLUMN_RUN_ID));
            long startDate = getLong(getColumnIndex(COLUMN_RUN_START_DATE));
            run.setId(runId);
            run.setStartDate(new Date(startDate));
            return run;
        }
    }

    public static class LocationCursor extends CursorWrapper {
        public LocationCursor(Cursor cursor) {
            super(cursor);
        }

        public Location getLocation() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }

            String provider = getString(getColumnIndex(COLUMN_LOCATION_PROVIDER));
            Location loc = new Location(provider);
            loc.setLatitude(getDouble(getColumnIndex(COLUMN_LOCATION_LAT)));
            loc.setLongitude(getDouble(getColumnIndex(COLUMN_LOCATION_LNG)));
            loc.setAltitude(getDouble(getColumnIndex(COLUMN_LOCATION_ALT)));
            loc.setTime(getLong(getColumnIndex(COLUMN_LOCATION_RUN_TIME)));
            return loc;
        }
    }

}
