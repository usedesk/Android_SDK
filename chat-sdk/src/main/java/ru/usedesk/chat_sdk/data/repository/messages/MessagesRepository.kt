
package ru.usedesk.chat_sdk.data.repository.messages

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import com.google.gson.Gson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.data.repository.api.loader.MessageResponseConverter
import ru.usedesk.chat_sdk.entity.*
import java.util.*

internal class MessagesRepository(
    private val appContext: Context,
    private val gson: Gson,
    private val configuration: UsedeskChatConfiguration,
    private val messageResponseConverter: MessageResponseConverter
) : IUsedeskMessagesRepository {

    private var inited = false

    private val notSentMessages = hashMapOf<Long, NotSentMessage>()
    private var messageDraft = UsedeskMessageDraft()
    private var mutex = Mutex()

    private fun getSharedPreferences(userKey: String) = appContext.getSharedPreferences(
        PREF_NAME + userKey,
        Context.MODE_PRIVATE
    )

    override suspend fun addNotSentMessage(
        userKey: String,
        clientMessage: UsedeskMessageOwner.Client
    ) {
        mutex.withLock {
            val sharedPreferences = getSharedPreferences(userKey)
            sharedPreferences.initIfNeeded()

            if (configuration.cacheMessagesWithFile || clientMessage is UsedeskMessageClientText) {
                val message = toMessage(clientMessage)

                notSentMessages[message.localId] = message

                sharedPreferences.edit {
                    putString(NOT_SENT_MESSAGES_KEY, gson.toJson(notSentMessages.values))
                }
            }
        }
    }

    override suspend fun removeNotSentMessage(
        userKey: String,
        localId: Long
    ) {
        mutex.withLock {
            getSharedPreferences(userKey).run {
                initIfNeeded()

                notSentMessages.remove(localId)

                edit {
                    putString(NOT_SENT_MESSAGES_KEY, gson.toJson(notSentMessages.values))
                }
            }
        }
    }

    override suspend fun getNotSentMessages(userKey: String) = mutex.withLock {
        getSharedPreferences(userKey).initIfNeeded()
        notSentMessages.values.map(this@MessagesRepository::toClientMessage)
    }

    override suspend fun setDraft(userKey: String, messageDraft: UsedeskMessageDraft) {
        mutex.withLock {
            getSharedPreferences(userKey).run {
                initIfNeeded()

                val oldMessageDraft = this@MessagesRepository.messageDraft
                this@MessagesRepository.messageDraft = messageDraft

                edit {
                    if (oldMessageDraft.text != messageDraft.text) {
                        putString(DRAFT_TEXT_KEY, messageDraft.text)
                    }
                    if (configuration.cacheMessagesWithFile && oldMessageDraft.files != messageDraft.files) {
                        putStringSet(
                            DRAFT_FILES_KEY, messageDraft.files.map {
                                it.uri.toString()
                            }.toSet()
                        )
                    }
                }
            }
        }
    }

    override suspend fun getDraft(userKey: String) = mutex.withLock {
        val sharedPreferences = getSharedPreferences(userKey)
        sharedPreferences.initIfNeeded()
        messageDraft
    }

    override suspend fun getNextLocalId() = mutex.withLock {
        var lastLocalId = -10000L
        getSharedPreferences("").run {
            edit {
                lastLocalId = getLong(LOCAL_ID_KEY, lastLocalId) - 1
                putLong(LOCAL_ID_KEY, lastLocalId)
            }
        }

        lastLocalId
    }

    private fun SharedPreferences.initIfNeeded() {
        if (!inited) {
            inited = true

            val notSentMessagesJson = getString(NOT_SENT_MESSAGES_KEY, null)

            if (notSentMessagesJson != null) {
                val notSentMessagesArray =
                    gson.fromJson(notSentMessagesJson, Array<NotSentMessage>::class.java)
                notSentMessagesArray.forEach {
                    notSentMessages[it.localId] = it
                    it.localId to it
                }
            }
            val draftText = getString(DRAFT_TEXT_KEY, "") ?: ""
            val draftFiles = getStringSet(DRAFT_FILES_KEY, setOf()) ?: setOf()
            messageDraft = UsedeskMessageDraft(
                draftText,
                draftFiles.mapNotNull {
                    try {
                        val uri = Uri.parse(it)
                        UsedeskFileInfo.create(
                            appContext,
                            uri
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            )
        }
    }

    private fun toMessage(clientMessageClient: UsedeskMessageOwner.Client): NotSentMessage =
        when (clientMessageClient) {
            is UsedeskMessageClientText -> NotSentMessage(
                clientMessageClient.localId,
                text = clientMessageClient.text
            )
            is UsedeskMessageClientVideo -> NotSentMessage(
                clientMessageClient.localId,
                video = fileToJson(clientMessageClient.file)
            )
            is UsedeskMessageClientAudio -> NotSentMessage(
                clientMessageClient.localId,
                audio = fileToJson(clientMessageClient.file)
            )
            is UsedeskMessageClientImage -> NotSentMessage(
                clientMessageClient.localId,
                image = fileToJson(clientMessageClient.file)
            )
            is UsedeskMessageClientFile -> NotSentMessage(
                clientMessageClient.localId,
                file = fileToJson(clientMessageClient.file)
            )
        }

    private fun toClientMessage(notSentMessage: NotSentMessage): UsedeskMessageOwner.Client =
        when {
            notSentMessage.text != null -> UsedeskMessageClientText(
                notSentMessage.localId,
                Calendar.getInstance(),
                notSentMessage.text,
                messageResponseConverter.convertText(notSentMessage.text),
                UsedeskMessageOwner.Client.Status.SEND_FAILED
            )
            notSentMessage.image != null -> UsedeskMessageClientImage(
                notSentMessage.localId,
                Calendar.getInstance(),
                jsonToFile(notSentMessage.image),
                UsedeskMessageOwner.Client.Status.SEND_FAILED
            )
            notSentMessage.video != null -> UsedeskMessageClientVideo(
                notSentMessage.localId,
                Calendar.getInstance(),
                jsonToFile(notSentMessage.video),
                UsedeskMessageOwner.Client.Status.SEND_FAILED
            )
            notSentMessage.audio != null -> UsedeskMessageClientAudio(
                notSentMessage.localId,
                Calendar.getInstance(),
                jsonToFile(notSentMessage.audio),
                UsedeskMessageOwner.Client.Status.SEND_FAILED
            )
            notSentMessage.file != null -> UsedeskMessageClientFile(
                notSentMessage.localId,
                Calendar.getInstance(),
                jsonToFile(notSentMessage.file),
                UsedeskMessageOwner.Client.Status.SEND_FAILED
            )
            else -> throw RuntimeException("Empty message")
        }

    private fun fileToJson(file: UsedeskFile): String = gson.toJson(file)

    private fun jsonToFile(fileJson: String): UsedeskFile = gson.fromJson(
        fileJson,
        UsedeskFile::class.java
    )

    companion object {
        private const val PREF_NAME = "UsedeskMessagesRepository"

        private const val LOCAL_ID_KEY = "localIdKey"
        private const val NOT_SENT_MESSAGES_KEY = "notSentMessagesKey"
        private const val DRAFT_TEXT_KEY = "draftTextKey"
        private const val DRAFT_FILES_KEY = "draftFilesKey"
    }

    private class NotSentMessage(
        val localId: Long,
        val text: String? = null,
        val file: String? = null,
        val image: String? = null,
        val audio: String? = null,
        val video: String? = null
    )
}