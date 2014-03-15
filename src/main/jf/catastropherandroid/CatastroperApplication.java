package main.jf.catastropherandroid;

import android.app.Application;
import android.net.http.AndroidHttpClient;
import com.google.android.gms.maps.model.LatLng;

public class CatastroperApplication extends Application {

    private AndroidHttpClient androidHttpClient;

    private String userFBAuthToken;

    private LatLng currentLatLng;

    public AndroidHttpClient getHttpClient() {
        if (androidHttpClient == null) {
            androidHttpClient = AndroidHttpClient.newInstance("Android");
        }

        return androidHttpClient;
    }

    public String getUserFBAuthToken() {
        return userFBAuthToken;
    }

    public void setUserFBAuthToken(String userFBAuthToken) {
        this.userFBAuthToken = userFBAuthToken;
    }

    public LatLng getCurrentLatLng() {
        return currentLatLng;
    }

    public void setCurrentLatLng(LatLng currentLatLng) {
        this.currentLatLng = currentLatLng;
    }
}