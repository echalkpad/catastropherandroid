package main.jf.catastropherandroid;

import android.content.Context;
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

    private final List<String> weatherMetaData = new LinkedList<String>();

    public Report(String title, String text, LatLng location) {
        this.title = title;
        this.text = text;
        this.location = location;
        this.timestamp = null;
        this.id = null;
    }

    public Report(String json, boolean onlyReport, Context context) throws JSONException {
        this(new JSONObject(json), onlyReport, context);

    }

    public Report(JSONObject jsonObject, boolean onlyReport, Context context) {
        try {
            JSONObject reportObject = onlyReport ? jsonObject : jsonObject.getJSONObject("report");
            JSONObject data = reportObject.getJSONObject("data");
            JSONObject loc = reportObject.getJSONObject("loc");
            this.title = data.getString("title");
            this.text = data.getString("text");
            this.timestamp = new Date(data.getLong("timestamp"));
            this.location = new LatLng(loc.getDouble("lat"), loc.getDouble("lon"));
            if (reportObject.has("_id")) {
                this.id = reportObject.getString("_id");
            } else {
                this.id = null;
            }
            if (!onlyReport && jsonObject.has("weather")) {
                JSONObject weather = jsonObject.getJSONObject("weather");
                addWeather(weather, context);
            }
        } catch (JSONException e) {
            throw new RuntimeException("JSON parsing in Report.java error!!");
        }
    }

    public void addWeather(JSONObject weather, Context context) throws JSONException {
        String msl = context.getString(R.string.weather_msl) + " " +
                weather.getDouble("msl") + " " +
                context.getString(R.string.weather_msl_unit);
        weatherMetaData.add(msl);

        String t = context.getString(R.string.weather_t) + " " +
                weather.getDouble("t") + " " +
                context.getString(R.string.weather_t_unit);
        weatherMetaData.add(t);

        String vis = context.getString(R.string.weather_vis) + " " +
                weather.getInt("vis") + " " +
                context.getString(R.string.weather_vis_unit);
        weatherMetaData.add(vis);

        String wd = context.getString(R.string.weather_wd) + " " +
                weather.getInt("wd") + " " +
                context.getString(R.string.weather_wd_unit);
        weatherMetaData.add(wd);

        String ws = context.getString(R.string.weather_ws) + " " +
                weather.getDouble("ws") + " " +
                context.getString(R.string.weather_ws_unit);
        weatherMetaData.add(ws);

        String r = context.getString(R.string.weather_r) + " " +
                weather.getInt("r") + " " +
                context.getString(R.string.weather_r_unit);
        weatherMetaData.add(r);

        String tstm = context.getString(R.string.weather_tstm) + " " +
                weather.getInt("tstm") + " " +
                context.getString(R.string.weather_tstm_unit);
        weatherMetaData.add(tstm);

        String gust = context.getString(R.string.weather_gust) + " " +
                weather.getDouble("gust") + " " +
                context.getString(R.string.weather_gust_unit);
        weatherMetaData.add(gust);

        String pis = context.getString(R.string.weather_pis) + " " +
                weather.getDouble("pis") + " " +
                context.getString(R.string.weather_pis_unit);
        weatherMetaData.add(pis);

        int pcatLevel = weather.getInt("pcat");

        String pcat = context.getString(R.string.weather_pcat) + " ";
        switch (pcatLevel) {
            case 0:
                pcat += context.getString(R.string.weather_pcat_unit_0);
                break;
            case 1:
                pcat += context.getString(R.string.weather_pcat_unit_1);
                break;
            case 2:
                pcat += context.getString(R.string.weather_pcat_unit_2);
                break;
            case 3:
                pcat += context.getString(R.string.weather_pcat_unit_3);
                break;
            case 4:
                pcat += context.getString(R.string.weather_pcat_unit_4);
                break;
            case 5:
                pcat += context.getString(R.string.weather_pcat_unit_5);
                break;
            case 6:
                pcat += context.getString(R.string.weather_pcat_unit_6);
                break;
        }

        weatherMetaData.add(pcat);
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
        in.readStringList(weatherMetaData);
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
        parcel.writeDouble(location.latitude);
        parcel.writeDouble(location.longitude);
        if (timestamp == null) {
            parcel.writeLong(-1);
        } else {
            parcel.writeLong(timestamp.getTime());
        }

        parcel.writeString(id);
        parcel.writeStringList(weatherMetaData);
    }

    public List<String> getMetaDataList(Context context) {
        List<String> metaDataList = new LinkedList<String>();
        String dateString = context.getString(R.string.view_report_date_prefix) + getTimestamp().toGMTString();
        dateString = dateString.substring(0, dateString.length() - 4);
        metaDataList.add(dateString);
        metaDataList.add(getText());
        metaDataList.addAll(weatherMetaData);
        return metaDataList;
    }

    public static List<Report> jsonToListOfReports(String json, boolean onlyReport, Context context) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (!jsonObject.getBoolean("status")) return null;
            JSONArray reportArray = jsonObject.getJSONArray("result");
            List<Report> reports = new LinkedList<Report>();
            for (int i = 0; i < reportArray.length(); ++i) {
                JSONObject reportJSON = reportArray.getJSONObject(i);
                Report report = new Report(reportJSON, onlyReport, context);
                reports.add(report);
            }

            return reports;
        } catch (JSONException e) {
            throw new RuntimeException("Report.java jsonToListOfReports error!!");
        }
    }
}
