package ru.usedesk.chat_sdk.data.repository._extra.retrofit

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import ru.usedesk.chat_sdk.data.repository.api.entity.OfflineFormRequest

internal interface IHttpApi {
    @POST("widget.js/post/")
    fun sendOfflineForm(@Body request: OfflineFormRequest): Call<ResponseBody>

    @Multipart
    @POST("send_file")
    fun postFile(@Part parts: List<MultipartBody.Part>): Call<ResponseBody>
}