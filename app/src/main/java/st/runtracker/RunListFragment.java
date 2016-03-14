package st.runtracker;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import st.runtracker.database.RunDatabaseHelper;
import st.runtracker.location.RunManager;
import st.runtracker.model.Run;

/**
 * Created by tengsun on 3/14/16.
 */
public class RunListFragment extends ListFragment {

    private RunDatabaseHelper.RunCursor cursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cursor = RunManager.getRunManager(getActivity()).queryRuns();

        // create an adapter to point at this cursor
        RunCursorAdapter adapter = new RunCursorAdapter(getActivity(), cursor);
        setListAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        cursor.close();
        super.onDestroy();
    }

    private static class RunCursorAdapter extends CursorAdapter {
        private RunDatabaseHelper.RunCursor runCursor;

        public RunCursorAdapter(Context context, RunDatabaseHelper.RunCursor cursor) {
            super(context, cursor, 0);
            this.runCursor = cursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Run run = runCursor.getRun();

            // get text view and format message
            TextView startDate = (TextView) view;
            String cellText = context.getString(R.string.tracker_cell_text, run.getStartDate());
            startDate.setText(cellText);
        }
    }
}
