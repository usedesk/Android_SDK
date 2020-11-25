package ru.usedesk.chat_sdk.internal.data.framework._extra.retrofit

import ru.usedesk.chat_sdk.internal.data.framework.httpapi.IHttpApi

interface IHttpApiFactory {
    fun getInstance(baseUrl: String): IHttpApi
}