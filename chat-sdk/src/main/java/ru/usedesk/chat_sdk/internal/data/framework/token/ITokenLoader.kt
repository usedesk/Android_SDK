package ru.usedesk.chat_sdk.internal.data.framework.token

interface ITokenLoader {
    fun loadData(): String?

    fun getData(): String

    fun setData(data: String?)

    fun clearData()
}