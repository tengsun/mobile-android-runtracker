package st.runtracker.database;

import android.content.Context;

import st.runtracker.location.RunManager;
import st.runtracker.model.Run;

/**
 * Created by tengsun on 3/15/16.
 */
public class RunLoader extends DataLoader<Run> {

    private long runId;

    public RunLoader(Context context, long runId) {
        super(context);
        this.runId = runId;
    }

    @Override
    public Run loadInBackground() {
        return RunManager.getRunManager(getContext()).getRun(runId);
    }
}
