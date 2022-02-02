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

    suspend fun getCachedFile(uri: Uri): Deferred<Uri>

    fun removeFileFromCache(uri: Uri)

    suspend fun setMessageDraft(messageDraft: UsedeskMessageDraft, cacheFiles: Boolean)

    fun getMessageDraft(): UsedeskMessageDraft

    fun getNextLocalId(): Long
}