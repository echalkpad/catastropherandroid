package main.jf.catastropherandroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.support.v4.app.NotificationCompat;
import com.google.android.gcm.GCMRegistrar;

public class GCMUtils {

    public static final String APP_VERSION_REGID_KEY = "main.jf.catastroperandroid.AppGCMVersionCode";

    public static final String GCM_REG_ID_KEY = "main.jf.catastropherandroid.GCMRegId";

    public static final String SHOW_GCM_DIALOG_ACTION = "main.jf.catastroperandroid.SHOW_GCM_DIALOG_ACTION";

    public static final String EXTRA_MESSAGE = "main.jf.catastroperandroid.EXTRA_MESSAGE";

    private GCMUtils() {
    }

    public static void sendGCMErrorMessageBroadcast(Context context,
                                                    String message) {
        Intent intent = new Intent(SHOW_GCM_DIALOG_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }

    public static void setRegistrationIdAppVersion(Context context,
                                                   String appVersionCode) {
        PreferencesUtils.setGCMAppRegVersion(context,
                appVersionCode);
    }

    public static String getRegistrationIdAppVersion(Context context) {
        return PreferencesUtils.getGCMAppRegVersion(context);
    }

    public static void removeRegistrationIdAppVersion(Context context) {
        PreferencesUtils.removeGCMAppRegVersion(context);
    }

    public static void setGCMRegId(Context context, String regId) {
        PreferencesUtils.setGCMRegId(context, regId);
    }

    public static String getGCMRegId(Context context) {
        return PreferencesUtils.getGCMRegId(context);
    }

    public static void removeGCMRegId(Context context) {
        PreferencesUtils.removeGCMRegId(context);
    }

    public static void register(Context context, String regId, String fbAccessToken, AndroidHttpClient androidHttpClient) {
        HttpHandler httpHandler = new HttpHandler(androidHttpClient);
        httpHandler.registerGCMId(context, fbAccessToken, regId);
    }

    public static void hasRegistered(Context context, String regId) {
        setGCMRegId(context, regId);
        GCMRegistrar.setRegisteredOnServer(context, true);
    }

    public static void unRegister(Context context, String userFbId, AndroidHttpClient androidHttpClient) {
        HttpHandler httpHandler = new HttpHandler(androidHttpClient);
        boolean result = httpHandler.unregisterGCMId(context, userFbId);

        if (result) {
            GCMRegistrar.setRegisteredOnServer(context, false);
            removeGCMRegId(context);
        }
    }

    public static void setupReportPUSH(Context context, Report report) {

        Intent notificationIntent = new Intent(context,
                MainActivity.class);

        notificationIntent.putExtra(context.getString(R.string.push_report_key), report);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        setupAndShowNotification(context, notificationIntent, report.getTitle());
    }

    private static void setupAndShowNotification(Context context,
                                                 Intent notificationIntent,
                                                 String title) {
        long when = System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.push_title)).
                        setSmallIcon(R.drawable.notification_icon).setWhen(when)
                .setContentIntent(pendingIntent).setContentText(title);

        Notification notification = builder.build();

        notification.flags = Notification.DEFAULT_LIGHTS
                | Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        int id = (int) when % Integer.MAX_VALUE;
        notificationManager.notify(id, notification);
    }
}