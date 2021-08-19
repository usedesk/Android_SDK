package ru.usedesk.chat_sdk.domain

import android.net.Uri
import ru.usedesk.chat_sdk.entity.UsedeskMessageClient
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft

internal interface ICachedMessagesInteractor {

    fun getNotSentMessages(): List<UsedeskMessageClient>

    fun addNotSentMessage(notSentMessage: UsedeskMessageClient)

    fun removeNotSentMessage(notSentMessage: UsedeskMessageClient)

    fun getCachedUri(uri: Uri): Uri

    fun addFileToCache(uri: Uri): Uri

    fun removeFileFromCache(uri: Uri)

    fun updateMessageDraft(now: Boolean)

    fun setMessageDraft(messageDraft: UsedeskMessageDraft, cacheFiles: Boolean)

    fun getMessageDraft(): UsedeskMessageDraft

    fun getNextLocalId(): Long
}