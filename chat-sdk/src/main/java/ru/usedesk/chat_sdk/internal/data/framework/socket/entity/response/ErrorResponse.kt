package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response

class ErrorResponse : BaseResponse(TYPE) {
    var message: String? = null
    var code = 0

    companion object {
        const val TYPE = "@@redbone/ERROR"
    }
}