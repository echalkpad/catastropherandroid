package main.jf.catastropherandroid;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

public class MyReportsActivity extends ListActivity {

    private ProgressDialog progressBar;

    private MyReportsArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_reports_activity);

        getActionBar().setTitle(R.string.my_reports_name);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        CatastroperApplication catastroperApplication = (CatastroperApplication) getApplication();

        setupAndShowProgressBar();
        HttpHandler httpHandler = new HttpHandler(catastroperApplication.getHttpClient());
        httpHandler.getMyReports(this, catastroperApplication.getUserFBAuthToken());
    }

    public void setupAndShowProgressBar() {
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage(getString(R.string.loading));
        progressBar.show();
    }

    public void dismissProgressBar() {
        progressBar.dismiss();
        progressBar = null;
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

    public void handleMyReportsResult(String json) {
        dismissProgressBar();
        if (json != null) {
            List<Report> reports = Report.jsonToListOfReports(json, true, this);
            if (reports == null) return;

            arrayAdapter = new MyReportsArrayAdapter(R.layout.my_reports_list_item, reports, this);
            setListAdapter(arrayAdapter);
        } else {
            Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void deleteReport(Report report) {
        setupAndShowProgressBar();
        CatastroperApplication catastroperApplication = (CatastroperApplication) getApplication();
        HttpHandler httpHandler = new HttpHandler(catastroperApplication.getHttpClient());
        httpHandler.deleteReport(this, report.getId(), catastroperApplication.getUserFBAuthToken(), report);
    }

    public void handleDeleteReportResult(String result, Report report) {
        if (JSONHandler.parsePostSuccessResult(result)) {
            Toast.makeText(this, getString(R.string.delete_report_successfull), Toast.LENGTH_SHORT).show();
            arrayAdapter.remove(report);
        } else {
            Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }

    }
}
