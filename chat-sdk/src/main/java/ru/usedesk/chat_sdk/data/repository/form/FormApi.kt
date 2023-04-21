
package ru.usedesk.chat_sdk.data.repository.form

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import ru.usedesk.chat_sdk.data.repository.form.entity.LoadForm
import ru.usedesk.chat_sdk.data.repository.form.entity.SaveForm

internal interface FormApi {
    @POST("v1/widget/field_list")
    fun loadForm(@Body body: LoadForm.Request): Call<ResponseBody>

    @POST("/v1/widget/custom_form/save")
    fun saveForm(@Body body: SaveForm.Request): Call<ResponseBody>
}