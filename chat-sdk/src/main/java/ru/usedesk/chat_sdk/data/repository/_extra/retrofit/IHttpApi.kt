package ru.usedesk.chat_sdk.data.repository._extra.retrofit

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

internal interface IHttpApi {
    @POST("widget.js/post")
    fun sendOfflineForm(@Body json: JsonObject): Call<ResponseBody>

    @Multipart
    @POST("send_file")
    fun postFile(@Part parts: List<MultipartBody.Part>): Call<ResponseBody>
}