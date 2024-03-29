package main.jf.catastropherandroid;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONHandler {

    private JSONHandler() {
        throw new RuntimeException("Should never be instantiated!");
    }

    public static boolean parsePostSuccessResult(String result) {
        if (result == null) return false;
        try {
            JSONObject resultJSON = new JSONObject(result);
            return resultJSON.getBoolean("status");
        } catch (JSONException e) {
            throw new RuntimeException("Error!");
        }
    }

    public static String GCMIdToJSON(String regId) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject device = new JSONObject();
            device.put("id", regId);
            jsonObject.put("device", device);
            return jsonObject.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Error!");
        }
    }
}
