package ru.usedesk.chat_sdk.data.repository._extra.retrofit

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import ru.usedesk.chat_sdk.data.repository.api.loader.apifile.entity.FileResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.apiofflineform.entity.OfflineFormRequest

internal interface IHttpApi {
    @POST("post/")
    fun postOfflineForm(@Body request: OfflineFormRequest): Call<Array<Any>>

    @Multipart
    @POST("send_file")
    fun postFile(@Part parts: List<MultipartBody.Part>): Call<FileResponse>
}