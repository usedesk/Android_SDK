package ru.usedesk.chat_sdk.data.repository.configuration.loader.token

internal interface ITokenLoader {
    fun loadData(): String?

    fun getDataNullable(): String?

    fun clearData()
}