package main.jf.catastropherandroid;

import android.app.Activity;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpHandler {

    private final AndroidHttpClient httpClient;

    private AsyncTask newReportAsyncTask;

    private AsyncTask registerGCMIdAsyncTask;

    private AsyncTask getReportsAsyncTask;

    private AsyncTask getMyReportsAsyncTask;

    private AsyncTask deleteReportAsyncTask;

    public HttpHandler(AndroidHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void sendNewReport(Report report, NewReportActivity newReportActivity, String fbAuthToken) {
        newReportActivity.setupAndShowProgressBar();
        String url = URLUtils.addAccessTokenToURL(newReportActivity.getString(R.string.new_report_url), fbAuthToken);
        newReportAsyncTask = new NewReportAsyncTask(url,
                report.toJSONString(), newReportActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public String postData(String url, String data) {
        try {
            HttpPost httpPost = new HttpPost(url);
            HttpEntity postDataEntity = new StringEntity(data, "UTF-8");
            httpPost.setEntity(postDataEntity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse response = httpClient.execute(httpPost);
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

    public String getData(String url) {
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);
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

    private class NewReportAsyncTask extends AsyncTask<Void, Integer, String> {

        private final String url;

        private final String json;

        private final NewReportActivity newReportActivity;

        public NewReportAsyncTask(String url, String json, NewReportActivity newReportActivity) {
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

    public void registerGCMIdAsync(MainActivity mainActivity, String fbAuthToken, String regId) {
        registerGCMIdAsyncTask = new RegisterGCMIdAsyncTask(
                URLUtils.addAccessTokenToURL(mainActivity.getString(R.string.gcm_register_url), fbAuthToken),
                JSONHandler.GCMIdToJSON(regId), regId, mainActivity).
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void registerGCMId(Context context, String fbAuthToken, String regId) {
        String url = URLUtils.addAccessTokenToURL(context.getString(R.string.gcm_register_url), fbAuthToken);
        String json = JSONHandler.GCMIdToJSON(regId);

        String result = postData(url, json);

        if (JSONHandler.parsePostSuccessResult(result)) {
            GCMUtils.hasRegistered(context, regId);
        }
    }

    public boolean unregisterGCMId(Context context, String fbAuthToken) {
        String url = URLUtils.addAccessTokenToURL(context.getString(R.string.gcm_unregister_url), fbAuthToken);
        String result = postData(url, "");

        return JSONHandler.parsePostSuccessResult(result);
    }

    private class RegisterGCMIdAsyncTask extends AsyncTask<Void, Integer, String> {

        private final String url;

        private final String json;

        private final String regId;

        private final MainActivity mainActivity;

        public RegisterGCMIdAsyncTask(String url, String json, String regId, MainActivity mainActivity) {
            this.url = url;
            this.json = json;
            this.regId = regId;
            this.mainActivity = mainActivity;
        }

        @Override
        protected String doInBackground(Void... v) {
            return postData(url, json);
        }

        @Override
        protected void onPostExecute(String result) {
            if (mainActivity != null && JSONHandler.parsePostSuccessResult(result)) {
                GCMUtils.hasRegistered(mainActivity, regId);
            }
        }
    }

    public static AndroidHttpClient getAndroidHttpClient(Activity activity) {
        CatastroperApplication app = (CatastroperApplication) activity.getApplication();
        return app.getHttpClient();
    }

    public void getReports(MainActivity mainActivity) {
        String url = mainActivity.getString(R.string.list_reports_url);
        getReportsAsyncTask = new GetReportsAsyncTask(url, mainActivity).
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class GetReportsAsyncTask extends AsyncTask<Void, Integer, String> {

        private final String url;

        private final MainActivity mainActivity;

        public GetReportsAsyncTask(String url, MainActivity mainActivity) {
            this.url = url;
            this.mainActivity = mainActivity;
        }

        @Override
        protected String doInBackground(Void... v) {
            return getData(url);
        }

        @Override
        protected void onPostExecute(String result) {
            if (mainActivity != null) {
                mainActivity.handleRefreshMapResults(result);
            }
        }
    }

    public void getMyReports(MyReportsActivity myReportsActivity, String fbAuthToken) {
        String url = URLUtils.addAccessTokenToURL(myReportsActivity.getString(R.string.my_reports_url), fbAuthToken);
        getMyReportsAsyncTask = new GetMyReportsAsyncTask(url, myReportsActivity).
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class GetMyReportsAsyncTask extends AsyncTask<Void, Integer, String> {

        private final String url;

        private final MyReportsActivity myReportsActivity;

        public GetMyReportsAsyncTask(String url, MyReportsActivity myReportsActivity) {
            this.url = url;
            this.myReportsActivity = myReportsActivity;
        }

        @Override
        protected String doInBackground(Void... v) {
            return getData(url);
        }

        @Override
        protected void onPostExecute(String result) {
            if (myReportsActivity != null) {
                myReportsActivity.handleMyReportsResult(result);
            }
        }
    }

    public void deleteReport(MyReportsActivity myReportsActivity, String reportId, String fbAuthToken, Report report) {
        String url = myReportsActivity.getString(R.string.delete_report_url);
        url += "/" + reportId;
        url = URLUtils.addAccessTokenToURL(url, fbAuthToken);

        deleteReportAsyncTask = new DeleteReportAsyncTask(url, myReportsActivity, report).
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class DeleteReportAsyncTask extends AsyncTask<Void, Integer, String> {

        private final String url;

        private final Report report;

        private final MyReportsActivity myReportsActivity;

        public DeleteReportAsyncTask(String url, MyReportsActivity myReportsActivity, Report report) {
            this.url = url;
            this.myReportsActivity = myReportsActivity;
            this.report = report;
        }

        @Override
        protected String doInBackground(Void... v) {
            return postData(url, "");
        }

        @Override
        protected void onPostExecute(String result) {
            if (myReportsActivity != null) {
                myReportsActivity.dismissProgressBar();
                myReportsActivity.handleDeleteReportResult(result, report);
            }
        }
    }

    public void tearDown() {
        if (newReportAsyncTask != null) {
            newReportAsyncTask.cancel(true);
            newReportAsyncTask = null;
        }

        if (registerGCMIdAsyncTask != null) {
            registerGCMIdAsyncTask.cancel(true);
            registerGCMIdAsyncTask = null;
        }

        if (getReportsAsyncTask != null) {
            getReportsAsyncTask.cancel(true);
            getReportsAsyncTask = null;
        }

        if (getMyReportsAsyncTask != null) {
            getMyReportsAsyncTask.cancel(true);
            getMyReportsAsyncTask = null;
        }

        if (deleteReportAsyncTask != null) {
            deleteReportAsyncTask.cancel(true);
            deleteReportAsyncTask = null;
        }
    }
}
