package ru.usedesk.chat_sdk.data.repository.messages

import android.net.Uri
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft
import ru.usedesk.chat_sdk.entity.UsedeskMessageOwner

interface IUsedeskMessagesRepository {
    suspend fun addNotSentMessage(userKey: String, clientMessage: UsedeskMessageOwner.Client)
    suspend fun removeNotSentMessage(userKey: String, clientMessage: UsedeskMessageOwner.Client)
    suspend fun getNotSentMessages(userKey: String): List<UsedeskMessageOwner.Client>

    suspend fun setDraft(userKey: String, messageDraft: UsedeskMessageDraft)
    suspend fun getDraft(userKey: String): UsedeskMessageDraft

    suspend fun addFileToCache(uri: Uri): Uri
    suspend fun removeFileFromCache(uri: Uri)

    /**
     * Return a negative value
     */
    suspend fun getNextLocalId(userKey: String): Long
}