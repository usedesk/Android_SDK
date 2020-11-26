package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response

class SetEmailResponse : BaseResponse(TYPE) {
    val state: State? = null
    val isReset = false

    companion object {
        const val TYPE = "@@chat/current/SET"
    }
}