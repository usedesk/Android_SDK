package ru.usedesk.chat_sdk.external.entity

interface IUsedeskActionListener {
    fun onConnected()

    fun onChatItemReceived(chatItem: UsedeskChatItem)

    fun onChatItemsReceived(chatItems: List<UsedeskChatItem>)

    fun onFeedbackReceived()

    fun onOfflineFormExpected(chatConfiguration: UsedeskChatConfiguration)

    fun onDisconnected()

    fun onException(usedeskException: Exception)
}