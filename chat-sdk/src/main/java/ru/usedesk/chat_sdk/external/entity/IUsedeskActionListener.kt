package ru.usedesk.chat_sdk.external.entity

import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException

interface IUsedeskActionListener {
    fun onConnected()

    fun onMessageReceived(message: UsedeskMessage)

    fun onMessagesReceived(messages: List<UsedeskMessage>)

    fun onFeedbackReceived()

    fun onOfflineFormExpected(chatConfiguration: UsedeskChatConfiguration)

    fun onDisconnected()

    fun onException(usedeskException: UsedeskException)
}