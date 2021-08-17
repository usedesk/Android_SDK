package ru.usedesk.chat_sdk.data.repository.messages

import ru.usedesk.chat_sdk.entity.UsedeskMessageClient
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft

interface IUsedeskMessagesRepository {
    fun addNotSentMessage(clientMessage: UsedeskMessageClient)
    fun removeNotSentMessage(clientMessage: UsedeskMessageClient)
    fun getNotSentMessages(): List<UsedeskMessageClient>

    fun setDraft(messageDraft: UsedeskMessageDraft)
    fun getDraft(): UsedeskMessageDraft?
}