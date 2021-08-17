package ru.usedesk.chat_sdk.data.repository.messages

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import ru.usedesk.chat_sdk.entity.*
import toothpick.InjectConstructor
import java.util.*

@InjectConstructor
internal class UsedeskMessagesRepository(
    private val appContext: Context,
    private val gson: Gson
) : IUsedeskMessagesRepository {

    private val sharedPreferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private var inited = false

    private val notSentMessages = hashMapOf<Long, NotSentMessage>()
    private var draft = UsedeskMessageDraft()

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

        draft = messageDraft

        sharedPreferences.edit()
            .putString(DRAFT_TEXT_KEY, messageDraft.text)
            .putStringSet(DRAFT_FILES_KEY, messageDraft.files.map {
                it.uri.toString()
            }.toSet()).apply()
    }

    @Synchronized
    override fun getDraft(): UsedeskMessageDraft {
        initIfNeeded()

        return draft
    }

    private fun initIfNeeded() {
        if (!inited) {
            inited = true

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
            draft = UsedeskMessageDraft(
                draftText,
                draftFiles.map {
                    val uri = Uri.parse(it)
                    UsedeskFileInfo.create(
                        appContext,
                        uri
                    )//TODO: Доступ по uri протухает после перезапуска приложения
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
            is UsedeskMessageClientFile -> NotSentMessage(
                clientMessageClient.localId,
                file = fileToJson(clientMessageClient.file)
            )
            is UsedeskMessageClientImage -> NotSentMessage(
                clientMessageClient.localId,
                image = fileToJson(clientMessageClient.file)
            )//TODO: глянуть где выпадет ошибка
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
            notSentMessage.file != null -> {
                val file = jsonToFile(notSentMessage.file)
                UsedeskMessageClientFile(
                    notSentMessage.localId,
                    Calendar.getInstance(),
                    file,
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
            else -> {//TODO: глянуть где выпадет ошибка
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