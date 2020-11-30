package ru.usedesk.chat_sdk.external.entity

import ru.usedesk.chat_sdk.external.entity.chat.UsedeskChatItem
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException

interface IUsedeskActionListener {
    fun onConnected()

    @Deprecated("Use new data class UsedeskChatItem")
    fun onMessageReceived(message: UsedeskMessage) {
    }

    @Deprecated("Use new data class UsedeskChatItem")
    fun onMessagesReceived(messages: List<UsedeskMessage>) {
    }

    fun onChatItemReceived(chatItem: UsedeskChatItem)

    fun onChatItemsReceived(chatItems: List<UsedeskChatItem>)

    fun onFeedbackReceived()

    fun onOfflineFormExpected(chatConfiguration: UsedeskChatConfiguration)

    fun onDisconnected()

    fun onException(usedeskException: UsedeskException)
}