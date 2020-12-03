package ru.usedesk.chat_sdk.data.framework.httpapi

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import ru.usedesk.chat_sdk.data.repository.api._entity.request.OfflineFormRequest

interface IHttpApi {
    @POST("post/")
    fun postOfflineForm(@Body request: OfflineFormRequest): Call<Array<Any>>
}