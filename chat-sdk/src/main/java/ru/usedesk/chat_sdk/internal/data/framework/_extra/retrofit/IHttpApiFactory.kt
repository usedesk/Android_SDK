package ru.usedesk.chat_sdk.internal.data.framework._extra.retrofit;

import androidx.annotation.NonNull;

import ru.usedesk.chat_sdk.internal.data.framework.httpapi.IHttpApi;

public interface IHttpApiFactory {

    @NonNull
    IHttpApi getInstance(@NonNull String baseUrl);
}
