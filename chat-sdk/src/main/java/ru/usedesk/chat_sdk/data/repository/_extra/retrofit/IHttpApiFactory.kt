package ru.usedesk.chat_sdk.data.repository._extra.retrofit

internal interface IHttpApiFactory {
    fun getInstance(key: String): IHttpApi
}