package ru.usedesk.chat_sdk.internal.data.framework.httpapi;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ru.usedesk.chat_sdk.external.entity.UsedeskOfflineForm;
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.entity.OfflineFormResponse;

public interface IHttpApi {
    @POST("/token")
    Call<OfflineFormResponse> postOfflineForm(@Body UsedeskOfflineForm offlineForm);
}
