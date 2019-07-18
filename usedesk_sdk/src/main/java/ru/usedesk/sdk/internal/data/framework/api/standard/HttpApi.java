package ru.usedesk.sdk.internal.data.framework.api.standard;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;

import ru.usedesk.sdk.external.entity.chat.OfflineForm;
import ru.usedesk.sdk.internal.utils.LogUtils;

public class HttpApi {

    private static final String TAG = HttpApi.class.getSimpleName();

    private static final String ENCODING = "UTF-8";

    private final Gson gson;

    @Inject
    HttpApi(Gson gson) {
        this.gson = gson;
    }

    public boolean post(String urlString, OfflineForm offlineForm) {
        try {
            String postData = new JSONObject(gson.toJson(offlineForm)).toString();

            //urlString = "https://secure.usedesk.ru/widget.js/post";

            URL url = new URL(urlString);

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
            boolean success = responseCode == HttpsURLConnection.HTTP_OK;

            LogUtils.LOGD(TAG, "SUCCESS: " + success);

            return success;
        } catch (Exception e) {
            LogUtils.LOGE(TAG, e);
        }

        return false;
    }
}