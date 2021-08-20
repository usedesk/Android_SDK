package ru.usedesk.chat_sdk.data.repository.configuration.loader.token

internal interface ITokenLoader {
    fun loadData(): String?

    fun getData(): String

    fun getDataNullable(): String?

    fun setData(data: String?)

    fun clearData()
}