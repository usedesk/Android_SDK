package ru.usedesk.chat_sdk.internal.data.framework.multipart;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import ru.usedesk.chat_sdk.internal.data.framework.file.entity.LoadedFile;

public class MultipartConverter implements IMultipartConverter {

    @Inject
    public MultipartConverter() {
    }

    @Override
    public MultipartBody.Part convert(String key, String value) {
        return MultipartBody.Part.createFormData(key, value);
    }

    @Override
    public MultipartBody.Part convert(String key, LoadedFile loadedFile) {
        MediaType mediaType = MediaType.get(loadedFile.getType());
        RequestBody requestBody = RequestBody.create(mediaType, loadedFile.getBytes());
        return MultipartBody.Part.createFormData(key, loadedFile.getName(), requestBody);
    }
}