package ru.usedesk.chat_sdk.internal.data.framework.api.apifile;

import androidx.annotation.NonNull;

import java.util.List;

import okhttp3.MultipartBody;
import ru.usedesk.chat_sdk.internal.data.framework.api.apifile.entity.FileResponse;

public interface IFileApi {
    @NonNull
    FileResponse post(@NonNull String baseUrl,
                      @NonNull List<MultipartBody.Part> request) throws Exception;
}