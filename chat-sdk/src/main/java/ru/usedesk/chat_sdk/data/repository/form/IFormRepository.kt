package ru.usedesk.chat_sdk.data.repository.form

import androidx.annotation.CheckResult
import ru.usedesk.chat_sdk.entity.UsedeskForm

internal interface IFormRepository {
    suspend fun saveForm(form: UsedeskForm)

    @CheckResult
    suspend fun loadForm(
        urlChatApi: String,
        clientToken: String,
        form: UsedeskForm
    ): LoadFormResponse

    sealed interface LoadFormResponse {
        class Done(val form: UsedeskForm) : LoadFormResponse
        class Error(val error: Int? = null) : LoadFormResponse
    }
}