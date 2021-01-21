package ru.usedesk.chat_sdk.internal.data.framework.api.apifile;

import androidx.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import okhttp3.MultipartBody;
import retrofit2.Response;
import ru.usedesk.chat_sdk.internal.data.framework.api.apifile.entity.FileResponse;
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.IHttpApi;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException;
import ru.usedesk.common_sdk.internal.api.IUsedeskApiFactory;

public class FileApi implements IFileApi {

    private final IUsedeskApiFactory apiFactory;

    @Inject
    public FileApi(@NonNull IUsedeskApiFactory apiFactory) {
        this.apiFactory = apiFactory;
    }

    @Override
    @NonNull
    public FileResponse post(@NonNull String baseUrl,
                             @NonNull List<MultipartBody.Part> request) throws Exception {
        IHttpApi httpApi = apiFactory.getInstance(baseUrl, IHttpApi.class);
        Response<FileResponse> response = httpApi.postFile(request).execute();
        if (response.isSuccessful() && response.code() == 200) {
            return response.body();
        }
        throw new UsedeskHttpException("Server error: " + response.code());
    }
}