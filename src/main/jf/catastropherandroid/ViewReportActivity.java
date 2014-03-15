package main.jf.catastropherandroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.MarkerOptions;

public class ViewReportActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_report_activity);

        Report report = getIntent().getParcelableExtra(getString(R.string.report_key));

        getActionBar().setTitle(report.getTitle());
        getActionBar().setDisplayHomeAsUpEnabled(true);

        GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.view_report_map))
                .getMap();

        map.addMarker(new MarkerOptions().
                position(report.getLocation())
                .title(report.getTitle())
                .snippet(report.getText()));


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(report.getLocation())
                .zoom(10)
                .build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        ListView listView = (ListView) findViewById(R.id.view_report_listview);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                R.layout.custom_list_item,
                report.getMetaDataList(this));
        listView.setAdapter(arrayAdapter);
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
