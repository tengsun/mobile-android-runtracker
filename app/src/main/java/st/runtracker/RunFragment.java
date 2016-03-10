package st.runtracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by tengsun on 3/10/16.
 */
public class RunFragment extends Fragment {

    private Button startButton, stopButton;
    private TextView startTextView, latTextView, lngTextView, altTextView, durationTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_run, container, false);

        startTextView = (TextView) view.findViewById(R.id.tracker_started_text);
        latTextView = (TextView) view.findViewById(R.id.tracker_lat_text);
        lngTextView = (TextView) view.findViewById(R.id.tracker_lng_text);
        altTextView = (TextView) view.findViewById(R.id.tracker_alt_text);
        durationTextView = (TextView) view.findViewById(R.id.tracker_elapsed_time_text);
        startButton = (Button) view.findViewById(R.id.tracker_start_button);
        stopButton = (Button) view.findViewById(R.id.tracker_stop_button);

        return view;
    }
}