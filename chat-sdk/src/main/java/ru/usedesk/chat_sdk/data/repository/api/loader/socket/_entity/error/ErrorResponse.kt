package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.error

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseResponse

internal class ErrorResponse : BaseResponse() {
    var message: String? = null
    var code: Int? = null

    companion object {
        const val TYPE = "@@redbone/ERROR"
    }
}