package ru.usedesk.chat_sdk.internal.data.framework.multipart;

import okhttp3.MultipartBody;
import ru.usedesk.chat_sdk.internal.data.framework.file.entity.LoadedFile;

public interface IMultipartConverter {
    MultipartBody.Part convert(String key, String value);

    MultipartBody.Part convert(String key, LoadedFile loadedFile);
}