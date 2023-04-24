
package ru.usedesk.chat_sdk.data.repository.form

import androidx.annotation.CheckResult
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText

internal interface IFormRepository {
    suspend fun saveForm(form: UsedeskForm)

    fun validateForm(form: UsedeskForm): UsedeskForm

    @CheckResult
    suspend fun loadForm(
        urlChatApi: String,
        clientToken: String,
        formId: Long,
        fieldsInfo: List<UsedeskMessageAgentText.FieldInfo>
    ): LoadFormResponse

    @CheckResult
    suspend fun sendForm(
        urlChatApi: String,
        clientToken: String,
        form: UsedeskForm
    ): SendFormResponse

    sealed interface SendFormResponse {
        class Done(val form: UsedeskForm) : SendFormResponse
        class Error(val error: Int? = null) : SendFormResponse
    }

    sealed interface LoadFormResponse {
        class Done(val form: UsedeskForm) : LoadFormResponse
        class Error(val error: Int? = null) : LoadFormResponse
    }
}