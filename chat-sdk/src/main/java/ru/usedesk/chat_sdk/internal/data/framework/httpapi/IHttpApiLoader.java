package ru.usedesk.chat_sdk.internal.data.framework.httpapi;

import androidx.annotation.NonNull;

import java.io.IOException;

import ru.usedesk.chat_sdk.external.entity.UsedeskOfflineForm;

public interface IHttpApiLoader {
    void post(@NonNull String baseUrl, @NonNull UsedeskOfflineForm offlineForm) throws IOException;
}
