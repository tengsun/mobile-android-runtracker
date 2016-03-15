package st.runtracker.database;

import android.content.Context;
import android.database.Cursor;

import st.runtracker.location.RunManager;

/**
 * Created by tengsun on 3/15/16.
 */
public class LocationListCursorLoader extends SQLiteCursorLoader {

    private long runId;

    public LocationListCursorLoader(Context context, long runId) {
        super(context);
        this.runId = runId;
    }

    @Override
    protected Cursor loadCursor() {
        return RunManager.getRunManager(getContext()).queryLocationsForRun(runId);
    }
}
