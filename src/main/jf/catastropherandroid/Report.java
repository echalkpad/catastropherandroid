package main.jf.catastropherandroid;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

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

    private final String title;

    private final String text;

    private final String userFbId;

    private final LatLng location;

    private final Date timestamp;

    public Report(String title, String text, String userFbId, LatLng location) {
        this.title = title;
        this.text = text;
        this.userFbId = userFbId;
        this.location = location;
        this.timestamp = null;
    }

    public Report(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONObject loc = jsonObject.getJSONObject("loc");
            this.title = data.getString("title");
            this.text = data.getString("text");
            this.timestamp = new Date(data.getLong("timestamp") / 1000); // fel
            this.userFbId = null;
            this.location = new LatLng(loc.getDouble("lon"), loc.getDouble("long"));
        } catch (JSONException e) {
            throw new RuntimeException("JSON parsing in Report.java error!!");
        }
    }

    public Report(Parcel in) {
        title = in.readString();
        text = in.readString();
        userFbId = in.readString();
        location = new LatLng(in.readDouble(), in.readDouble());
        long timestampLong = in.readLong();
        if (timestampLong == -1) {
            timestamp = null;
        } else {
            timestamp = new Date(timestampLong);
        }
    }

    public String toJSONString() {
        JSONObject report;
        try {
            report = new JSONObject();
            JSONObject loc = new JSONObject();
            loc.put("lon", location.longitude);
            loc.put("lat", location.latitude);
            report.put("loc", loc);
            JSONObject user = new JSONObject();
            user.put("facebook_id", userFbId);
            report.put("user", user);
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

    public String getUserFbId() {
        return userFbId;
    }

    public LatLng getLocation() {
        return location;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(text);
        parcel.writeString(userFbId);
        parcel.writeDouble(location.longitude);
        parcel.writeDouble(location.latitude);
        if (timestamp == null) {
            parcel.writeLong(-1);
        } else {
            parcel.writeLong(timestamp.getTime());
        }
    }
}
