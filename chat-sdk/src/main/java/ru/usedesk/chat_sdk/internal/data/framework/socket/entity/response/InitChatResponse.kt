package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response

class InitChatResponse : BaseResponse(TYPE) {
    val token: String? = null
    val setup: Setup? = null
    val noOperators: Boolean? = null

    companion object {
        const val TYPE = "@@chat/current/INITED"
    }
}