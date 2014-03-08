package main.jf.catastropherandroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * About activity for Catastropher.
 *
 * @author Johan Stenberg
 */
public class AboutActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.about_activity);

        getActionBar().setTitle(getString(R.string.about_activity_name));
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
