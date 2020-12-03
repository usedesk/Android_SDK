package ru.usedesk.chat_sdk.data.repository.api._entity.response

internal class ErrorResponse : BaseResponse() {
    var message: String? = null
    var code: Int? = null

    companion object {
        const val TYPE = "@@redbone/ERROR"
    }
}