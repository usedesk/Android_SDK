package ru.usedesk.chat_sdk.data.repository.messages

import android.net.Uri
import kotlinx.coroutines.Deferred
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft
import ru.usedesk.chat_sdk.entity.UsedeskMessageOwner

internal interface ICachedMessagesRepository {

    fun getNotSentMessages(): List<UsedeskMessageOwner.Client>

    fun addNotSentMessage(notSentMessage: UsedeskMessageOwner.Client)

    fun updateNotSentMessage(notSentMessage: UsedeskMessageOwner.Client)

    fun removeNotSentMessage(notSentMessage: UsedeskMessageOwner.Client)

    suspend fun getCachedFileAsync(uri: Uri): Deferred<Uri>

    suspend fun removeFileFromCache(uri: Uri)

    /**
     * Returns old UsedeskMessageDraft value
     */
    suspend fun setMessageDraft(
        messageDraft: UsedeskMessageDraft,
        cacheFiles: Boolean
    ): UsedeskMessageDraft

    suspend fun getMessageDraft(): UsedeskMessageDraft

    fun getNextLocalId(): Long
}