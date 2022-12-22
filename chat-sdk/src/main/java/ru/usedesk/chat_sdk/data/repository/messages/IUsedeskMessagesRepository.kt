package ru.usedesk.chat_sdk.data.repository.messages

import android.net.Uri
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft
import ru.usedesk.chat_sdk.entity.UsedeskMessageOwner

interface IUsedeskMessagesRepository {
    fun addNotSentMessage(userKey: String, clientMessage: UsedeskMessageOwner.Client)
    fun removeNotSentMessage(userKey: String, clientMessage: UsedeskMessageOwner.Client)
    fun getNotSentMessages(userKey: String): List<UsedeskMessageOwner.Client>

    fun setDraft(userKey: String, messageDraft: UsedeskMessageDraft)
    fun getDraft(userKey: String): UsedeskMessageDraft

    fun addFileToCache(uri: Uri): Uri
    fun removeFileFromCache(uri: Uri)

    /**
     * Return a negative value
     */
    fun getNextLocalId(userKey: String): Long
}