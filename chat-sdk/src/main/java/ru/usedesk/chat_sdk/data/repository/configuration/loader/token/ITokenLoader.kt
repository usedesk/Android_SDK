package ru.usedesk.chat_sdk.data.repository.configuration.loader.token

interface ITokenLoader {
    fun loadData(): String?

    fun getData(): String

    fun setData(data: String?)

    fun clearData()
}