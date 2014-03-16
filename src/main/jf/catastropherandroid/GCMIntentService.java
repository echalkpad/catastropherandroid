package main.jf.catastropherandroid;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.net.http.AndroidHttpClient;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;

public class GCMIntentService extends GCMBaseIntentService {

    private enum GCMError {
        ACCOUNT_MISSING,
        AUTHENTICATION_FAILED
    }

    @Override
    protected void onError(Context context, String errorId) {
        GCMError error = GCMError.valueOf(errorId);

        switch (error) {
            case ACCOUNT_MISSING:
            case AUTHENTICATION_FAILED:
                GCMUtils.sendGCMErrorMessageBroadcast(context, context
                        .getString(R.string.gcm_error_google_account));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        String fbAccessToken = getFBAccessToken();
        if (fbAccessToken == null) return;

        int KMToPUSHAtMost = Integer.parseInt(PreferencesUtils.KMAtMostFromPUSH(context));
        if (KMToPUSHAtMost == -1) return;

        String data = intent.getStringExtra("data");
        Report report;
        try {
            report = new Report(data, false, this);
        } catch (JSONException e) {
            throw new RuntimeException("Swag");
        }

        LatLng latest = getLatestLatLng();
        LatLng reportLatLong = report.getLocation();

        float[] results = new float[10];
        if (latest != null) {
            Location.distanceBetween(latest.latitude, latest.longitude, reportLatLong.latitude, reportLatLong.longitude, results);
        }
        int distanceKM = (int) results[0] / 1000;

        if (distanceKM <= KMToPUSHAtMost) {
            GCMUtils.setupReportPUSH(context, report);
        }
    }

    @Override
    protected void onRegistered(Context context, String regId) {
        String packageName = getPackageName();
        try {
            PackageManager packageManager = getPackageManager();

            PackageInfo packageInfo;
            if (packageManager != null) {
                packageInfo = packageManager.getPackageInfo(
                        packageName, 0);
            } else {
                throw new IllegalStateException("PackageManager is null!");
            }

            String appVersionCode = Integer.toString(packageInfo.versionCode);

            GCMUtils.setRegistrationIdAppVersion(context, appVersionCode);
        } catch (NameNotFoundException e) {
        }

        String fbAccessToken = getFBAccessToken();
        if (fbAccessToken != null) {
            GCMUtils.register(context, regId, fbAccessToken, getHttpClient());
        }
    }

    @Override
    protected void onUnregistered(Context context, String regId) {
        if (regId.isEmpty()) {
            regId = GCMUtils.getGCMRegId(context);
        }

        if (regId != null && !regId.isEmpty()) {
            GCMUtils.removeRegistrationIdAppVersion(context);

            if (GCMRegistrar.isRegisteredOnServer(context)) {
                String userFbId = getFBAccessToken();
                if (userFbId != null) {
                    GCMUtils.unRegister(context, userFbId, getHttpClient());
                }
            }
        }
    }

    @Override
    protected String[] getSenderIds(Context context) {
        String googleProjectId = getString(R.string.google_project_id);
        return new String[]{googleProjectId};
    }

    private String getFBAccessToken() {
        CatastroperApplication app = (CatastroperApplication) getApplication();

        if (app != null) {
            return app.getUserFBAuthToken();
        } else {
            throw new IllegalStateException("Null App");
        }
    }

    private AndroidHttpClient getHttpClient() {
        CatastroperApplication app = (CatastroperApplication) getApplication();

        if (app != null) {
            return app.getHttpClient();
        } else {
            throw new IllegalStateException("Null App");
        }
    }

    private LatLng getLatestLatLng() {
        CatastroperApplication app = (CatastroperApplication) getApplication();

        if (app != null) {
            return app.getCurrentLatLng();
        } else {
            throw new IllegalStateException("Null App");
        }
    }
}