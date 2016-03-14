package st.runtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    private Button startButton, stopButton;
    private TextView startedTextView, latTextView, lngTextView, altTextView, durationTextView;

    // location receiver
    private BroadcastReceiver locationReceiver = new LocationReceiver() {
        @Override
        protected void onLocationReceived(Context context, Location loc) {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // init run manager
        runManager = RunManager.getRunManager(getActivity());
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
                run = runManager.startNewRun();
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
        stopButton.setEnabled(started);
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

}
