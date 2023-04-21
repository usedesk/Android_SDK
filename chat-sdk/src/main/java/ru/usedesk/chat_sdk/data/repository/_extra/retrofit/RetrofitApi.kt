
package ru.usedesk.chat_sdk.data.repository._extra.retrofit

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import ru.usedesk.chat_sdk.data.repository.api.entity.SendAdditionalFields

internal interface RetrofitApi {
    @POST("widget.js/post")
    fun sendOfflineForm(@Body json: JsonObject): Call<ResponseBody>

    @Multipart
    @POST("uapi/v1/send_file")
    fun postFile(@Part parts: List<MultipartBody.Part>): Call<ResponseBody>

    @POST("uapi/v1/addFieldsToChat")
    fun postAdditionalFields(@Body body: SendAdditionalFields.Request): Call<ResponseBody>

    @GET("uapi/chat/getChatMessage")
    fun loadPreviousMessages(
        @Query("chat_token") chatToken: String,
        @Query("comment_id") commentId: Long
    ): Call<ResponseBody>

    @Multipart
    @POST("v1/chat/setClient")
    fun setClient(@Part parts: List<MultipartBody.Part>): Call<ResponseBody>

    @Multipart
    @POST("uapi/v2/createChat")
    fun createChat(@Part parts: List<MultipartBody.Part>): Call<ResponseBody>
}