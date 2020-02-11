package ru.usedesk.chat_sdk.internal.data.framework.httpapi;

import androidx.annotation.NonNull;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Response;
import ru.usedesk.chat_sdk.external.entity.UsedeskOfflineForm;
import ru.usedesk.chat_sdk.internal.data.framework._extra.retrofit.HttpApiFactory;
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.entity.OfflineFormResponse;

public class HttpApiLoader implements IHttpApiLoader {
    private final HttpApiFactory httpApiFactory;

    @Inject
    HttpApiLoader(@NonNull HttpApiFactory httpApiFactory) {
        this.httpApiFactory = httpApiFactory;
    }

    @Override
    public void post(@NonNull String baseUrl, @NonNull UsedeskOfflineForm offlineForm) throws IOException {
        IHttpApi httpApi = httpApiFactory.getInstance(baseUrl);
        Response<OfflineFormResponse> response = httpApi.postOfflineForm(offlineForm)
                .execute();

        if (response.isSuccessful() && response.body() != null) {
            OfflineFormResponse offlineFormResponse = response.body();
            return;
        }
        throw new IOException("Server error: " + response.code());
    }
}
