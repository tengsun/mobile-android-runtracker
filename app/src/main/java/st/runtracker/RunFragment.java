package st.runtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import st.runtracker.database.RunLoader;
import st.runtracker.location.LocationReceiver;
import st.runtracker.location.RunManager;
import st.runtracker.model.Run;

/**
 * Created by tengsun on 3/10/16.
 */
public class RunFragment extends Fragment {

    private RunManager runManager;
    private Run run;
    private Location lastLocation;
    private static final String ARG_RUN_ID = "RUN_ID";
    private static final int LOAD_RUN = 0;

    private Button startButton, stopButton;
    private TextView startedTextView, latTextView, lngTextView, altTextView, durationTextView;

    // location receiver
    private BroadcastReceiver locationReceiver = new LocationReceiver() {
        @Override
        protected void onLocationReceived(Context context, Location loc) {
            if (!runManager.isTrackingRun(run)) {
                return;
            }

            lastLocation = loc;
            if (isVisible()) {
                updateUI();
            }
        }

        @Override
        protected void onProviderEnabledChanged(boolean enabled) {
            int msg = enabled ? R.string.tracker_gps_enabled : R.string.tracker_gps_disabled;
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
        }
    };

    public static RunFragment newInstance(long runId) {
        Bundle args = new Bundle();
        args.putLong(ARG_RUN_ID, runId);
        RunFragment rf = new RunFragment();
        rf.setArguments(args);
        return rf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // init run manager
        runManager = RunManager.getRunManager(getActivity());

        // check for a run id as argument
        Bundle args = getArguments();
        if (args != null) {
            long runId = args.getLong(ARG_RUN_ID, -1);
            if (runId != -1) {
                LoaderManager lm = getLoaderManager();
                lm.initLoader(LOAD_RUN, args, new RunLoaderCallbacks());
                lastLocation = runManager.getLastLocationForRun(runId);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_run, container, false);

        // bind views
        startedTextView = (TextView) view.findViewById(R.id.tracker_started_text);
        latTextView = (TextView) view.findViewById(R.id.tracker_lat_text);
        lngTextView = (TextView) view.findViewById(R.id.tracker_lng_text);
        altTextView = (TextView) view.findViewById(R.id.tracker_alt_text);
        durationTextView = (TextView) view.findViewById(R.id.tracker_elapsed_time_text);
        startButton = (Button) view.findViewById(R.id.tracker_start_button);
        stopButton = (Button) view.findViewById(R.id.tracker_stop_button);

        // bind events
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (run == null) {
                    run = runManager.startNewRun();
                } else {
                    runManager.startTrackingRun(run);
                }
                updateUI();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runManager.stopRun();
                updateUI();
            }
        });
        updateUI();

        return view;
    }

    private void updateUI() {
        boolean started = runManager.isTrackingRun();
        boolean trackingThisRun = runManager.isTrackingRun(run);

        if (run != null) {
            startedTextView.setText(run.getStartDate().toString());
        }

        int durationSeconds = 0;
        if (run != null && lastLocation != null) {
            durationSeconds = run.getDurationSeconds(lastLocation.getTime());
            latTextView.setText(Double.toString(lastLocation.getLatitude()));
            lngTextView.setText(Double.toString(lastLocation.getLongitude()));
            altTextView.setText(Double.toString(lastLocation.getAltitude()));
        }
        durationTextView.setText(Run.formatDuration(durationSeconds));

        startButton.setEnabled(!started);
        stopButton.setEnabled(started && trackingThisRun);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(locationReceiver,
                new IntentFilter(RunManager.ACTION_LOCATION));
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(locationReceiver);
        super.onStop();
    }

    // run data loader callback

    private class RunLoaderCallbacks implements LoaderManager.LoaderCallbacks<Run> {

        @Override
        public Loader<Run> onCreateLoader(int id, Bundle args) {
            return new RunLoader(getActivity(), args.getLong(ARG_RUN_ID));
        }

        @Override
        public void onLoadFinished(Loader<Run> loader, Run data) {
            run = data;
            updateUI();
        }

        @Override
        public void onLoaderReset(Loader<Run> loader) {
            // do nothing
        }
    }

}
