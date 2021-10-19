package ru.usedesk.chat_sdk.data.repository.messages

import android.net.Uri
import ru.usedesk.chat_sdk.entity.UsedeskMessageClient
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft

interface IUsedeskMessagesRepository {
    fun addNotSentMessage(userKey: String, clientMessage: UsedeskMessageClient)
    fun removeNotSentMessage(userKey: String, clientMessage: UsedeskMessageClient)
    fun getNotSentMessages(userKey: String): List<UsedeskMessageClient>

    fun setDraft(userKey: String, messageDraft: UsedeskMessageDraft)
    fun getDraft(userKey: String): UsedeskMessageDraft

    fun addFileToCache(uri: Uri): Uri
    fun removeFileFromCache(uri: Uri)

    /**
     * Return a negative value
     */
    fun getNextLocalId(userKey: String): Long
}