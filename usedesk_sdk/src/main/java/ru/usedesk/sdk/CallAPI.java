package ru.usedesk.sdk;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import ru.usedesk.sdk.utils.LogUtils;

class CallAPI {

    private static final String TAG = CallAPI.class.getSimpleName();

    private static final String ENCODING = "UTF-8";

    private CallAPI() {
    }

    static boolean post(String urlString, String postData) {
        URL url;
        boolean success = false;

        try {
            url = new URL(urlString);

            LogUtils.LOGD(TAG, "URL: " + url);
            LogUtils.LOGD(TAG, "Data: " + postData);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");

            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(outputStream, ENCODING));

            String encodedData = URLEncoder.encode(postData, ENCODING);
            LogUtils.LOGD(TAG, "Data (encoded): " + encodedData);
            bufferedWriter.write(encodedData);

            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            int responseCode = httpURLConnection.getResponseCode();
            success = responseCode == HttpsURLConnection.HTTP_OK;

            LogUtils.LOGD(TAG, "SUCCESS: " + success);
        } catch (Exception e) {
            LogUtils.LOGE(TAG, e);
        }

        return success;
    }
}