package ru.usedesk.chat_sdk.entity

interface IUsedeskActionListener {
    fun onConnectedState(connected: Boolean)

    fun onMessageReceived(message: UsedeskMessage)

    fun onNewMessageReceived(message: UsedeskMessage)

    fun onMessagesReceived(messages: List<UsedeskMessage>)

    fun onMessageUpdated(message: UsedeskMessage)

    fun onFeedbackReceived()

    fun onOfflineFormExpected(offlineFormSettings: UsedeskOfflineFormSettings)

    fun onException(usedeskException: Exception)
}