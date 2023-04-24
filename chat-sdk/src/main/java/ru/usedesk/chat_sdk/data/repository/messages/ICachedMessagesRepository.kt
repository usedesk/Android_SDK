
package ru.usedesk.chat_sdk.data.repository.messages

import android.net.Uri
import kotlinx.coroutines.Deferred
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft
import ru.usedesk.chat_sdk.entity.UsedeskMessageOwner

internal interface ICachedMessagesRepository {

    suspend fun getNotSentMessages(): List<UsedeskMessageOwner.Client>

    suspend fun addNotSentMessage(notSentMessage: UsedeskMessageOwner.Client)

    suspend fun updateNotSentMessage(notSentMessage: UsedeskMessageOwner.Client)

    suspend fun removeNotSentMessage(localId: Long)

    suspend fun getCachedFileAsync(uri: Uri): Deferred<Uri>

    suspend fun removeFileFromCache(uri: Uri)

    suspend fun createSendingMessage(
        fileInfo: UsedeskFileInfo,
        localId: Long
    ): UsedeskMessage.File

    /**
     * Returns old UsedeskMessageDraft value
     */
    suspend fun setMessageDraft(
        messageDraft: UsedeskMessageDraft,
        cacheFiles: Boolean
    ): UsedeskMessageDraft

    suspend fun getMessageDraft(): UsedeskMessageDraft

    suspend fun getNextLocalId(): Long
}