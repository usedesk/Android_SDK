package ru.usedesk.chat_sdk.internal.data.framework.retrofit;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;

import ru.usedesk.chat_sdk.external.entity.OfflineForm;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException;

public class HttpApi {//TODO retrofit
    private static final String ENCODING = "UTF-8";

    private final Gson gson;

    @Inject
    HttpApi(Gson gson) {
        this.gson = gson;
    }

    public boolean post(String urlString, OfflineForm offlineForm) throws UsedeskHttpException {
        try {
            String postData = new JSONObject(gson.toJson(offlineForm)).toString();

            URL url = new URL(urlString);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");

            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(outputStream, ENCODING));

            String encodedData = URLEncoder.encode(postData, ENCODING);
            bufferedWriter.write(encodedData);

            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            return httpURLConnection.getResponseCode() == HttpsURLConnection.HTTP_OK;
        } catch (JSONException e) {
            throw new UsedeskHttpException(UsedeskHttpException.Error.JSON_ERROR, e.getMessage());
        } catch (IOException e) {
            throw new UsedeskHttpException(UsedeskHttpException.Error.IO_ERROR, e.getMessage());
        }
    }
}