package main.jf.catastropherandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesUtils {

    private PreferencesUtils() {
        throw new RuntimeException("Should never be instantiated!");
    }

    public static boolean getAcceptsPush(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getBoolean(context.getString(R.string.allow_new_reports_push_key), true);
    }

    public static String KMAtMostFromPUSH(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString(context.getString(R.string.km_to_push_key), "-1");
    }

    public static String getGCMAppRegVersion(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString(GCMUtils.APP_VERSION_REGID_KEY, null);
    }

    public static void setGCMAppRegVersion(Context context, String verSion) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(GCMUtils.APP_VERSION_REGID_KEY, verSion);
        editor.commit();
    }

    public static void removeGCMAppRegVersion(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(GCMUtils.APP_VERSION_REGID_KEY);
        editor.commit();
    }

    public static String getGCMRegId(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getString(GCMUtils.GCM_REG_ID_KEY, null);
    }

    public static void setGCMRegId(Context context, String regId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(GCMUtils.GCM_REG_ID_KEY, regId);
        editor.commit();
    }

    public static void removeGCMRegId(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(GCMUtils.GCM_REG_ID_KEY);
        editor.commit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
