package ru.usedesk.chat_sdk.internal.data.framework.httpapi;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import ru.usedesk.chat_sdk.external.entity.UsedeskOfflineForm;
import ru.usedesk.chat_sdk.internal.data.framework.api.apifile.entity.FileResponse;

public interface IHttpApi {
    @POST("post/")
    Call<ResponseBody> postOfflineForm(@Body UsedeskOfflineForm offlineForm);

    @Multipart
    @POST("send_file")
    Call<FileResponse> postFile(@Part List<MultipartBody.Part> parts);
}
