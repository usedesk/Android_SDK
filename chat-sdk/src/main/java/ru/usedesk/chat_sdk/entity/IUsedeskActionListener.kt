package ru.usedesk.chat_sdk.entity

interface IUsedeskActionListener {
    fun onConnected()

    fun onMessageReceived(message: UsedeskMessage)

    fun onMessagesReceived(messages: List<UsedeskMessage>)

    fun onFeedbackReceived()

    fun onOfflineFormExpected(chatConfiguration: UsedeskChatConfiguration)

    fun onDisconnected()

    fun onException(usedeskException: Exception)
}