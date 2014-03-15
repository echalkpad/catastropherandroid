package main.jf.catastropherandroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class NewReportActivity extends Activity {

    private ProgressDialog progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.new_report_activity);

        final String userFBId = getIntent().getExtras().getString(getString(R.string.user_fb_id_key));
        double longitude = getIntent().getExtras().getDouble(getString(R.string.longitude_key));
        double latitude = getIntent().getExtras().getDouble(getString(R.string.latitude_key));
        float zoom = getIntent().getExtras().getFloat(getString(R.string.zoom_level_key));

        final LatLng location = new LatLng(latitude, longitude);

        GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.new_report_map))
                .getMap();


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
                .zoom(zoom)
                .build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        map.setMyLocationEnabled(true);

        final MarkerOptions markerOptions = new MarkerOptions().position(location);
        markerOptions.draggable(true);
        map.addMarker(markerOptions);

        getActionBar().setTitle(getString(R.string.new_report_name));
        getActionBar().setDisplayHomeAsUpEnabled(true);

        AndroidHttpClient httpClient = HttpHandler.getAndroidHttpClient(this);
        final HttpHandler httpHandler = new HttpHandler(httpClient);

        Button saveButton = (Button) findViewById(R.id.new_report_send_text);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String title = ((TextView) findViewById(R.id.new_report_title_text)).getText().toString();
                String text = ((TextView) findViewById(R.id.new_report_text)).getText().toString();
                if (title == null || title.equals("")) {
                    Toast.makeText(NewReportActivity.this, getString(R.string.new_report_empty_title), Toast.LENGTH_SHORT).show();
                } else if (text == null || text.equals("")) {
                    Toast.makeText(NewReportActivity.this, getString(R.string.new_report_empty_text), Toast.LENGTH_SHORT).show();
                } else {
                    LatLng markerLocation = markerOptions.getPosition();
                    Report report = new Report(text, title, markerLocation);
                    CatastroperApplication catastroperApplication = (CatastroperApplication) getApplication();
                    httpHandler.sendNewReport(report, NewReportActivity.this, catastroperApplication.getUserFBAuthToken());
                }
            }

        });
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

    public void setupAndShowProgressBar() {
        Button saveButton = (Button) findViewById(R.id.new_report_send_text);
        saveButton.setEnabled(false);
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage(getString(R.string.loading));
        progressBar.show();
    }

    public void dismissProgressBar() {
        Button saveButton = (Button) findViewById(R.id.new_report_send_text);
        saveButton.setEnabled(true);
        progressBar.dismiss();
        progressBar = null;
    }

    public void handleResult(String result) {
        if (JSONHandler.parsePostSuccessResult(result)) {
            Toast.makeText(NewReportActivity.this, getString(R.string.new_report_success), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(NewReportActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressBar != null) {
            progressBar.dismiss();
            progressBar = null;
        }
    }
}
