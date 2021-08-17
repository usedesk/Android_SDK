package ru.usedesk.chat_sdk.data.repository.messages

import android.net.Uri
import ru.usedesk.chat_sdk.entity.UsedeskMessageClient
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft

interface IUsedeskMessagesRepository {
    fun addNotSentMessage(clientMessage: UsedeskMessageClient)
    fun removeNotSentMessage(clientMessage: UsedeskMessageClient)
    fun getNotSentMessages(): List<UsedeskMessageClient>

    fun setDraft(messageDraft: UsedeskMessageDraft)
    fun getDraft(): UsedeskMessageDraft?

    fun addFileToCache(uri: Uri): Uri
    fun removeFileFromCache(uri: Uri)
}