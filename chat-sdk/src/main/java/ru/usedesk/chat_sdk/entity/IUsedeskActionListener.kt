package ru.usedesk.chat_sdk.entity

import ru.usedesk.chat_sdk.domain.IUsedeskChat

interface IUsedeskActionListener {
    fun onModel(
        model: IUsedeskChat.Model,
        newMessages: List<UsedeskMessage>,
        updatedMessages: List<UsedeskMessage>,
        removedMessages: List<UsedeskMessage>
    ) {
    }

    fun onException(usedeskException: Exception) {}
}