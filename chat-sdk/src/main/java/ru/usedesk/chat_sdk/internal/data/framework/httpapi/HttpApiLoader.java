package ru.usedesk.chat_sdk.internal.data.framework.httpapi;

import androidx.annotation.NonNull;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Response;
import ru.usedesk.chat_sdk.external.entity.UsedeskOfflineForm;
import ru.usedesk.common_sdk.internal.api.UsedeskApiFactory;

public class HttpApiLoader implements IHttpApiLoader {
    private final UsedeskApiFactory apiFactory;

    @Inject
    HttpApiLoader(@NonNull UsedeskApiFactory apiFactory) {
        this.apiFactory = apiFactory;
    }

    @Override
    public void post(@NonNull String baseUrl, @NonNull UsedeskOfflineForm offlineForm) throws IOException {
        IHttpApi httpApi = apiFactory.getInstance(baseUrl, IHttpApi.class);
        Response<ResponseBody> response = httpApi.postOfflineForm(offlineForm).execute();

        if (response.isSuccessful() && response.body() != null) {
            return;
        }
        throw new IOException("Server error: " + response.code());
    }
}
