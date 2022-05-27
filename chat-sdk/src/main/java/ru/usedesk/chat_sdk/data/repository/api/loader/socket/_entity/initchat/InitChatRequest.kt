package ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseRequest

internal class InitChatRequest(
    val token: String?,
    val companyId: String,
    val url: String,
    messageLimit: Int
) : BaseRequest(TYPE) {

    private val payload = Payload(messageLimit)

    class Payload(
        val messageLimit: Int
    ) {
        val sdk = VALUE_CURRENT_SDK
        val type = VALUE_TYPE_SDK
    }

    companion object {
        private const val TYPE = "@@server/chat/INIT"
        private const val VALUE_CURRENT_SDK = "android"
        private const val VALUE_TYPE_SDK = "sdk"
    }
}