package ru.usedesk.chat_sdk.data.repository.messages

import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft
import ru.usedesk.chat_sdk.entity.UsedeskMessageOwner

interface IUsedeskMessagesRepository {
    suspend fun addNotSentMessage(clientMessage: UsedeskMessageOwner.Client)

    suspend fun removeNotSentMessage(localId: String)

    suspend fun getNotSentMessages(): List<UsedeskMessageOwner.Client>

    suspend fun setDraft(messageDraft: UsedeskMessageDraft)

    suspend fun getDraft(): UsedeskMessageDraft

    /**
     * Return a negative value
     */
    suspend fun getNextLocalId(): String
}