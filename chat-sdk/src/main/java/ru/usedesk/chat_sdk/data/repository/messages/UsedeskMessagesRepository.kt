package ru.usedesk.chat_sdk.data.repository.messages

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.google.gson.Gson
import ru.usedesk.chat_sdk.data.repository.api.loader.file.IFileLoader
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_sdk.utils.UsedeskFileUtil
import toothpick.InjectConstructor
import java.io.File
import java.util.*
import kotlin.math.absoluteValue

@InjectConstructor
internal class UsedeskMessagesRepository(
    private val appContext: Context,
    private val gson: Gson,
    private val fileLoader: IFileLoader
) : IUsedeskMessagesRepository {

    private val sharedPreferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private var inited = false

    private val notSentMessages = hashMapOf<Long, NotSentMessage>()
    private var messageDraft = UsedeskMessageDraft()
    private var lastLocalId: Long = 0L

    @Synchronized
    override fun addNotSentMessage(clientMessage: UsedeskMessageClient) {
        initIfNeeded()

        val message = toMessage(clientMessage)

        notSentMessages[message.localId] = message

        sharedPreferences.edit()
            .putString(NOT_SENT_MESSAGES_KEY, gson.toJson(notSentMessages.values))
            .apply()
    }

    @Synchronized
    override fun removeNotSentMessage(clientMessage: UsedeskMessageClient) {
        initIfNeeded()

        notSentMessages.remove(clientMessage.localId)

        sharedPreferences.edit()
            .putString(NOT_SENT_MESSAGES_KEY, gson.toJson(notSentMessages.values))
            .apply()
    }

    @Synchronized
    override fun getNotSentMessages(): List<UsedeskMessageClient> {
        initIfNeeded()
        return notSentMessages.values.map {
            toClientMessage(it)
        }
    }

    @Synchronized
    override fun setDraft(messageDraft: UsedeskMessageDraft) {
        initIfNeeded()

        val oldMessageDraft = this.messageDraft
        this.messageDraft = messageDraft

        sharedPreferences.edit().apply {
            if (oldMessageDraft.text != messageDraft.text) {
                putString(DRAFT_TEXT_KEY, messageDraft.text)
            }
            if (oldMessageDraft.files != messageDraft.files) {
                putStringSet(
                    DRAFT_FILES_KEY, messageDraft.files.map {
                        it.uri.toString()
                    }.toSet()
                )
            }
        }.apply()
    }

    @Synchronized
    override fun getDraft(): UsedeskMessageDraft {
        initIfNeeded()

        return messageDraft
    }

    override fun addFileToCache(uri: Uri): Uri {
        return if (uri.toString().startsWith("file://" + appContext.cacheDir.absolutePath)) {
            uri
        } else {
            val ext = UsedeskFileUtil.getExtension(appContext, uri)
            val cachedName = uri.hashCode().absoluteValue.toString() + if (ext.isNotEmpty()) {
                ".$ext"
            } else {
                ""
            }
            val cachedFile = File(appContext.cacheDir.absolutePath, cachedName)
            try {
                val cachedUri = cachedFile.toUri()
                fileLoader.copy(uri, cachedUri)
                cachedUri
            } catch (e: Exception) {
                cachedFile.delete()
                throw e
            }
        }
    }

    override fun removeFileFromCache(uri: Uri) {
        File(uri.toString()).delete()
    }

    override fun getNextLocalId(): Long {
        initIfNeeded()

        lastLocalId--

        sharedPreferences.edit()
            .putLong(LOCAL_ID_KEY, lastLocalId)
            .apply()

        return lastLocalId
    }

    private fun initIfNeeded() {
        if (!inited) {
            inited = true

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
            is UsedeskMessageFile -> if (clientMessageClient.file.isImage()) {
                NotSentMessage(
                    clientMessageClient.localId,
                    image = fileToJson(clientMessageClient.file)
                )
            } else {
                NotSentMessage(
                    clientMessageClient.localId,
                    file = fileToJson(clientMessageClient.file)
                )
            }
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
        val image: String? = null
    )
}