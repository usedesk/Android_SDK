package ru.usedesk.chat_sdk.data.repository

import ru.usedesk.chat_sdk.di.chat.ChatScope
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import javax.inject.Inject

@ChatScope
class InitClientMessageRepository @Inject constructor(
    initChatConfiguration: UsedeskChatConfiguration,
) {
    var initClientMessage: String? = initChatConfiguration.clientInitMessage
    var offlineFormMessage: String? = null

    fun clearMessages() {
        initClientMessage = null
        offlineFormMessage = null
    }
}