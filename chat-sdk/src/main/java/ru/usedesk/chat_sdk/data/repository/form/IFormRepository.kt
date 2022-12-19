package ru.usedesk.chat_sdk.data.repository.form

import ru.usedesk.chat_sdk.entity.UsedeskForm

internal interface IFormRepository {
    suspend fun saveForm(form: UsedeskForm)

    suspend fun loadForm(form: UsedeskForm): UsedeskForm
}