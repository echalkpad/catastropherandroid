package main.jf.catastropherandroid;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpHandler {

    private static final String ANDROID_HTTP_CLIENT_TAG = "Android";

    private final HttpClient httpclient = AndroidHttpClient.newInstance(ANDROID_HTTP_CLIENT_TAG);

    private AsyncTask postAsyncTask;

    public void sendNewReport(Report report, NewReportActivity newReportActivity) {
        newReportActivity.setupAndShowProgressBar();
        postAsyncTask = new PostAsyncTask(newReportActivity.getString(R.string.new_report_url),
                report.toJSONString(), newReportActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String postData(String url, String data) {
        try {
            HttpPost httpPost = new HttpPost(url);
            HttpEntity postDataEntity = new StringEntity(data);
            httpPost.setEntity(postDataEntity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse response = httpclient.execute(httpPost);
            postDataEntity.consumeContent();
            if (response.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                response.getEntity().consumeContent();
                return result;
            } else {
                return null;
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        return null;
    }

    private class PostAsyncTask extends AsyncTask<Void, Integer, String> {

        private final String url;

        private final String json;

        private final NewReportActivity newReportActivity;

        public PostAsyncTask(String url, String json, NewReportActivity newReportActivity) {
            this.url = url;
            this.json = json;
            this.newReportActivity = newReportActivity;
        }

        @Override
        protected String doInBackground(Void... v) {
            return postData(url, json);
        }

        @Override
        protected void onPostExecute(String result) {
            if (newReportActivity != null) {
                newReportActivity.dismissProgressBar();
                newReportActivity.handleResult(result);
            }
        }
    }

    public void tearDown() {
        if (postAsyncTask != null) {
            postAsyncTask.cancel(true);
            postAsyncTask = null;
        }
    }
}
