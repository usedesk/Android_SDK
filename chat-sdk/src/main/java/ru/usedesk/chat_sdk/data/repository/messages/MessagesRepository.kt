package ru.usedesk.chat_sdk.data.repository.messages

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import ru.usedesk.chat_sdk.data.repository.api.loader.file.IFileLoader
import ru.usedesk.chat_sdk.entity.*
import java.io.File
import java.util.*

internal class MessagesRepository(
    private val appContext: Context,
    private val gson: Gson,
    private val fileLoader: IFileLoader,
    private val configuration: UsedeskChatConfiguration
) : IUsedeskMessagesRepository {

    private var inited = false

    private val notSentMessages = hashMapOf<Long, NotSentMessage>()
    private var messageDraft = UsedeskMessageDraft()
    private var lastLocalId: Long = 0L

    private fun getSharedPreferences(userKey: String): SharedPreferences {
        return appContext.getSharedPreferences(PREF_NAME + userKey, Context.MODE_PRIVATE)
    }

    @Synchronized
    override fun addNotSentMessage(userKey: String, clientMessage: UsedeskMessageClient) {
        initIfNeeded(userKey)

        if (configuration.cacheMessagesWithFile || clientMessage is UsedeskMessageClientText) {
            val message = toMessage(clientMessage)

            notSentMessages[message.localId] = message

            getSharedPreferences(userKey).edit()
                .putString(NOT_SENT_MESSAGES_KEY, gson.toJson(notSentMessages.values))
                .apply()
        }
    }

    @Synchronized
    override fun removeNotSentMessage(userKey: String, clientMessage: UsedeskMessageClient) {
        initIfNeeded(userKey)

        notSentMessages.remove(clientMessage.localId)

        getSharedPreferences(userKey).edit()
            .putString(NOT_SENT_MESSAGES_KEY, gson.toJson(notSentMessages.values))
            .apply()
    }

    @Synchronized
    override fun getNotSentMessages(userKey: String): List<UsedeskMessageClient> {
        initIfNeeded(userKey)
        return notSentMessages.values.map {
            toClientMessage(it)
        }
    }

    @Synchronized
    override fun setDraft(userKey: String, messageDraft: UsedeskMessageDraft) {
        initIfNeeded(userKey)

        val oldMessageDraft = this.messageDraft
        this.messageDraft = messageDraft

        getSharedPreferences(userKey).edit().apply {
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
        }.apply()
    }

    @Synchronized
    override fun getDraft(userKey: String): UsedeskMessageDraft {
        initIfNeeded(userKey)

        return messageDraft
    }

    override fun addFileToCache(uri: Uri): Uri {
        return fileLoader.toCache(uri)
    }

    override fun removeFileFromCache(uri: Uri) {
        File(uri.toString()).delete()
    }

    override fun getNextLocalId(userKey: String): Long {
        initIfNeeded(userKey)

        lastLocalId--

        getSharedPreferences(userKey).edit()
            .putLong(LOCAL_ID_KEY, lastLocalId)
            .apply()

        return lastLocalId
    }

    private fun initIfNeeded(userKey: String) {
        if (!inited) {
            inited = true

            val sharedPreferences = getSharedPreferences(userKey)

            lastLocalId = sharedPreferences.getLong(LOCAL_ID_KEY, 0L)

            val notSentMessagesJson = sharedPreferences.getString(NOT_SENT_MESSAGES_KEY, null)

            if (notSentMessagesJson != null) {
                val notSentMessagesArray =
                    gson.fromJson(notSentMessagesJson, Array<NotSentMessage>::class.java)
                notSentMessagesArray.forEach {
                    notSentMessages[it.localId] = it
                    it.localId to it
                }
            }
            val draftText = sharedPreferences.getString(DRAFT_TEXT_KEY, "") ?: ""
            val draftFiles = sharedPreferences.getStringSet(DRAFT_FILES_KEY, setOf()) ?: setOf()
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

    private fun toMessage(clientMessageClient: UsedeskMessageClient): NotSentMessage {
        return when (clientMessageClient) {
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
            else -> throw RuntimeException("Unknown client message class: ${clientMessageClient::class.java}")
        }
    }

    private fun toClientMessage(notSentMessage: NotSentMessage): UsedeskMessageClient {
        return when {
            notSentMessage.text != null -> {
                UsedeskMessageClientText(
                    notSentMessage.localId,
                    Calendar.getInstance(),
                    notSentMessage.text,
                    UsedeskMessageClient.Status.SEND_FAILED
                )
            }
            notSentMessage.image != null -> {
                val file = jsonToFile(notSentMessage.image)
                UsedeskMessageClientImage(
                    notSentMessage.localId,
                    Calendar.getInstance(),
                    file,
                    UsedeskMessageClient.Status.SEND_FAILED
                )
            }
            notSentMessage.video != null -> {
                val file = jsonToFile(notSentMessage.video)
                UsedeskMessageClientVideo(
                    notSentMessage.localId,
                    Calendar.getInstance(),
                    file,
                    UsedeskMessageClient.Status.SEND_FAILED
                )
            }
            notSentMessage.audio != null -> {
                val file = jsonToFile(notSentMessage.audio)
                UsedeskMessageClientAudio(
                    notSentMessage.localId,
                    Calendar.getInstance(),
                    file,
                    UsedeskMessageClient.Status.SEND_FAILED
                )
            }
            notSentMessage.file != null -> {
                val file = jsonToFile(notSentMessage.file)
                UsedeskMessageClientFile(
                    notSentMessage.localId,
                    Calendar.getInstance(),
                    file,
                    UsedeskMessageClient.Status.SEND_FAILED
                )
            }
            else -> {
                throw RuntimeException("Empty message")
            }
        }
    }

    private fun fileToJson(file: UsedeskFile): String {
        return gson.toJson(file)
    }

    private fun jsonToFile(fileJson: String): UsedeskFile {
        return gson.fromJson(fileJson, UsedeskFile::class.java)
    }

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