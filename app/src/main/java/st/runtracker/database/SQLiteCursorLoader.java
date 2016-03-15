package st.runtracker.database;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by tengsun on 3/15/16.
 */
public abstract class SQLiteCursorLoader extends AsyncTaskLoader<Cursor> {

    private Cursor cursor;
    protected abstract Cursor loadCursor();

    public SQLiteCursorLoader(Context context) {
        super(context);
    }

    @Override
    public Cursor loadInBackground() {
        Cursor cursor = loadCursor();
        if (cursor != null) {
            // ensure the content window is filled
            cursor.getCount();
        }
        return cursor;
    }

    @Override
    public void deliverResult(Cursor data) {
        Cursor oldCursor = cursor;
        cursor = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        // close old cursor if needed
        if (oldCursor != null && oldCursor != data && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    @Override
    protected void onStartLoading() {
        if (cursor != null) {
            deliverResult(cursor);
        }
        if (takeContentChanged() || cursor == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor data) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // ensure loader is stopped
        onStopLoading();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        cursor = null;
    }
}
