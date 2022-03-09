package ru.usedesk.chat_sdk.domain

import android.net.Uri
import kotlinx.coroutines.Deferred
import ru.usedesk.chat_sdk.entity.UsedeskMessageClient
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft

internal interface ICachedMessagesInteractor {

    fun getNotSentMessages(): List<UsedeskMessageClient>

    fun addNotSentMessage(notSentMessage: UsedeskMessageClient)

    fun updateNotSentMessage(notSentMessage: UsedeskMessageClient)

    fun removeNotSentMessage(notSentMessage: UsedeskMessageClient)

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