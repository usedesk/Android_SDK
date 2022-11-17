package ru.usedesk.chat_sdk.data.repository.api.entity

import com.google.gson.JsonObject

internal interface LoadFields {
    class Request(
        val chat: String,
        val ids: List<Long>
    )

    class Response(
        val status: Int?,
        val fields: Array<JsonObject>?
        //TODO: из-за того, что в ответе не массив, а набор полей, приходится парсить вручну
    )
}