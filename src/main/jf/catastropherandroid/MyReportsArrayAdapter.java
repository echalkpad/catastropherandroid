package main.jf.catastropherandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MyReportsArrayAdapter extends ArrayAdapter<Report> {

    private List<Report> reports;

    private MyReportsActivity myReportsActivity;

    public MyReportsArrayAdapter(int textViewResourceId, List<Report> reports, MyReportsActivity myReportsActivity) {
        super(myReportsActivity, textViewResourceId, reports);
        this.reports = reports;
        this.myReportsActivity = myReportsActivity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.my_reports_list_item, null);
        }

        final Report report = reports.get(position);

        if (report != null) {
            TextView titleTextView = (TextView) v.findViewById(R.id.my_report_title);
            TextView textTextView = (TextView) v.findViewById(R.id.my_report_text);
            if (titleTextView != null) {
                titleTextView.setText(report.getTitle());
            }
            if (textTextView != null) {
                textTextView.setText(report.getText());
            }
        }

        if (v == null) return v;

        v.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(myReportsActivity, ViewReportActivity.class);
                intent.putExtra(myReportsActivity.getString(R.string.report_key), report);
                myReportsActivity.startActivity(intent);
            }

        });

        Button deleteButton = (Button) v.findViewById(R.id.delete_report);

        deleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                myReportsActivity.deleteReport(report);
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(myReportsActivity);
                builder.setMessage(myReportsActivity.getString(R.string.delete_report_confirmation)).
                        setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();
            }

        });

        return v;
    }
}
