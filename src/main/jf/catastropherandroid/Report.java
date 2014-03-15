package main.jf.catastropherandroid;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Report implements Parcelable {

    public static final Parcelable.Creator<Report> CREATOR
            = new Parcelable.Creator<Report>() {
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        public Report[] newArray(int size) {
            return new Report[size];
        }
    };

    private final String id;

    private final String title;

    private final String text;

    private final LatLng location;

    private final Date timestamp;

    public Report(String title, String text, LatLng location) {
        this.title = title;
        this.text = text;
        this.location = location;
        this.timestamp = null;
        this.id = null;
    }

    public Report(String json) throws JSONException {
        this(new JSONObject(json));

    }

    public Report(JSONObject jsonObject) {
        try {
            JSONObject reportObject = jsonObject.getJSONObject("report");
            JSONObject data = reportObject.getJSONObject("data");
            JSONObject loc = reportObject.getJSONObject("loc");
            this.title = data.getString("title");
            this.text = data.getString("text");
            this.timestamp = new Date(data.getLong("timestamp") / 1000); // TODO fel
            this.location = new LatLng(loc.getDouble("lat"), loc.getDouble("lon"));
            if (reportObject.has("_id")) {
                this.id = reportObject.getString("_id");
            } else {
                this.id = null;
            }
        } catch (JSONException e) {
            throw new RuntimeException("JSON parsing in Report.java error!!");
        }
    }

    public Report(Parcel in) {
        title = in.readString();
        text = in.readString();
        location = new LatLng(in.readDouble(), in.readDouble());
        long timestampLong = in.readLong();
        if (timestampLong == -1) {
            timestamp = null;
        } else {
            timestamp = new Date(timestampLong);
        }
        id = in.readString();
    }

    public String toJSONString() {
        JSONObject report;
        try {
            report = new JSONObject();
            JSONObject loc = new JSONObject();
            loc.put("lon", location.longitude);
            loc.put("lat", location.latitude);
            report.put("loc", loc);
            JSONObject data = new JSONObject();
            data.put("title", title);
            data.put("text", text);
            report.put("data", data);
        } catch (JSONException e) {
            throw new RuntimeException("Report.java toJSONString error!!");
        }

        return report.toString();
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public LatLng getLocation() {
        return location;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(text);
        parcel.writeDouble(location.longitude);
        parcel.writeDouble(location.latitude);
        if (timestamp == null) {
            parcel.writeLong(-1);
        } else {
            parcel.writeLong(timestamp.getTime());
        }
        parcel.writeString(id);
    }

    public static List<Report> jsonToListOfReports(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (!jsonObject.getBoolean("status")) return null;
            JSONArray reportArray = jsonObject.getJSONArray("result");
            List<Report> reports = new LinkedList<Report>();
            for (int i = 0; i < reportArray.length(); ++i) {
                JSONObject reportJSON = reportArray.getJSONObject(i);
                Report report = new Report(reportJSON);
                reports.add(report);
            }

            return reports;
        } catch (JSONException e) {
            throw new RuntimeException("Report.java jsonToListOfReports error!!");
        }
    }
}
