package main.jf.catastropherandroid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtils {

    private PreferencesUtils() {
        throw new RuntimeException("Should never be instantiated!");
    }

    public static boolean getAcceptsPush(Activity activity) {
        SharedPreferences preferences = getSharedPreferences(activity);
        return preferences.getBoolean(activity.getString(R.string.allow_new_reports_push_key), true);
    }

    private static SharedPreferences getSharedPreferences(Activity activity) {
        return activity.getPreferences(Context.MODE_PRIVATE);
    }
}
