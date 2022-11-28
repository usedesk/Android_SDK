package ru.usedesk.chat_sdk.entity

import ru.usedesk.chat_sdk.domain.IUsedeskChat

interface IUsedeskActionListener {

    fun onModel(model: IUsedeskChat.Model) {}

    fun onConnectionState(connectionState: UsedeskConnectionState) {}

    fun onClientTokenReceived(clientToken: String) {}

    fun onMessageReceived(message: UsedeskMessage) {}

    fun onNewMessageReceived(message: UsedeskMessage) {}

    fun onMessagesReceived(messages: List<UsedeskMessage>) {}

    fun onMessageUpdated(message: UsedeskMessage) {}

    fun onMessageRemoved() {} //TODO:

    fun onFeedbackReceived() {} //TODO:

    fun onOfflineFormExpected(offlineFormSettings: UsedeskOfflineFormSettings) {}

    fun onException(usedeskException: Exception) {}
}