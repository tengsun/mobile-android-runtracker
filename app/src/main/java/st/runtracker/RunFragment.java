package st.runtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import st.runtracker.database.LastLocationLoader;
import st.runtracker.database.LocationListCursorLoader;
import st.runtracker.database.RunDatabaseHelper;
import st.runtracker.database.RunLoader;
import st.runtracker.location.LocationReceiver;
import st.runtracker.location.RunManager;
import st.runtracker.model.Run;
import st.runtracker.util.TimeUtil;

/**
 * Created by tengsun on 3/10/16.
 */
public class RunFragment extends Fragment {

    private static final String TAG = "RunFragment";

    private RunManager runManager;
    private Run run;
    private Location lastLocation;
    private RunDatabaseHelper.LocationCursor locationCursor;
    private static final String ARG_RUN_ID = "RUN_ID";
    private static final int LOAD_RUN = 0;
    private static final int LOAD_LOCATION = 1;
    private static final int LOAD_LOCATIONS = 2;

    private Button startButton, stopButton;
    private TextView startedTextView, latTextView, lngTextView, altTextView, durationTextView;
    private MapView mapView;

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

        // init baidu map
        SDKInitializer.initialize(getActivity().getApplicationContext());

        // init run manager
        runManager = RunManager.getRunManager(getActivity());

        // check for a run id as argument
        Bundle args = getArguments();
        if (args != null) {
            long runId = args.getLong(ARG_RUN_ID, -1);
            if (runId != -1) {
                LoaderManager lm = getLoaderManager();
                lm.initLoader(LOAD_RUN, args, new RunLoaderCallbacks());
                lm.initLoader(LOAD_LOCATION, args, new LastLocationLoaderCallbacks());
                lm.initLoader(LOAD_LOCATIONS, args, new LocationListLoaderCallbacks());
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
        mapView = (MapView) view.findViewById(R.id.tracker_map_view);

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
            startedTextView.setText(TimeUtil.getDisplayDatetime(run.getStartDate()));
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

    private void updateMap() {
        BaiduMap map = mapView.getMap();

        // iterate over the locations
        locationCursor.moveToFirst();

        // define the polyline
        List<LatLng> points = new ArrayList<LatLng>();
        while (!locationCursor.isAfterLast()) {
            Location loc = locationCursor.getLocation();
            LatLng point = new LatLng(loc.getLatitude(), loc.getLongitude());
            points.add(point);

            // check start/end point
            int resourceId = 0;
            if (locationCursor.isFirst()) {
                resourceId = R.drawable.start;
            } else if (locationCursor.isLast()) {
                resourceId = R.drawable.end;
            } else {
                resourceId = R.drawable.dot;
            }

            // add overlay to the map
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(resourceId);
            OverlayOptions marker = new MarkerOptions().position(point).icon(bitmap);
            map.addOverlay(marker);

            // Log.d(TAG, loc.getTime() + ": " + loc.getLatitude() + ", " + loc.getLongitude());
            locationCursor.moveToNext();
        }

        // add polyline to the map
        if (points.size() > 1) {
            OverlayOptions polyline = new PolylineOptions().points(points).color(Color.BLUE);
            map.addOverlay(polyline);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(locationReceiver,
                new IntentFilter(RunManager.ACTION_LOCATION));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(locationReceiver);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
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

    // last location data loader callback

    private class LastLocationLoaderCallbacks implements LoaderManager.LoaderCallbacks<Location> {

        @Override
        public Loader<Location> onCreateLoader(int id, Bundle args) {
            return new LastLocationLoader(getActivity(), args.getLong(ARG_RUN_ID));
        }

        @Override
        public void onLoadFinished(Loader<Location> loader, Location data) {
            lastLocation = data;
            updateUI();
        }

        @Override
        public void onLoaderReset(Loader<Location> loader) {
            // do nothing
        }
    }

    // location list data loader callback

    private class LocationListLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new LocationListCursorLoader(getActivity(), args.getLong(ARG_RUN_ID));
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            locationCursor = (RunDatabaseHelper.LocationCursor) data;
            updateMap();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // stop using the data
            locationCursor.close();
            locationCursor = null;
        }
    }

}
