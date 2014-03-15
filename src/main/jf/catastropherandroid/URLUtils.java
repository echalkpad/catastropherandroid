package main.jf.catastropherandroid;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class URLUtils {

    private URLUtils() {
        throw new RuntimeException("Should never be instantiated!");
    }

    public static String addAccessTokenToURL(String url, String fbAuthToken) {
        try {
            return url + "?access_token=" + URLEncoder.encode(fbAuthToken, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("swaghili");
        }
    }
}