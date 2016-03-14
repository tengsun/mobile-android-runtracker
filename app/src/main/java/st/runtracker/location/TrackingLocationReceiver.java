package st.runtracker.location;

import android.content.Context;
import android.location.Location;

/**
 * Created by tengsun on 3/14/16.
 */
public class TrackingLocationReceiver extends LocationReceiver {

    @Override
    protected void onLocationReceived(Context context, Location loc) {
        RunManager.getRunManager(context).insertLocation(loc);
    }

}
