package st.runtracker.database;

import android.content.Context;
import android.location.Location;

import st.runtracker.location.RunManager;

/**
 * Created by tengsun on 3/15/16.
 */
public class LastLocationLoader extends DataLoader<Location> {

    private long runId;

    public LastLocationLoader(Context context, long runId) {
        super(context);
        this.runId = runId;
    }

    @Override
    public Location loadInBackground() {
        return RunManager.getRunManager(getContext()).getLastLocationForRun(runId);
    }
}
