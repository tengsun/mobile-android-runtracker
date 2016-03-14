package st.runtracker;

import android.support.v4.app.Fragment;

/**
 * Created by tengsun on 3/14/16.
 */
public class RunListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new RunListFragment();
    }
}
