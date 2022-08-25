package ru.usedesk.chat_gui.chat.messages

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_gui.UsedeskViewModel
import java.io.File
import java.util.*

internal class MessagesViewModel : UsedeskViewModel<MessagesViewModel.Model>(Model()) {

    private val actionListenerRx: IUsedeskActionListenerRx
    private val usedeskChat: IUsedeskChat = UsedeskChatSdk.requireInstance()

    private var messages = listOf<UsedeskMessage>()
    private var chatItems = listOf<ChatItem>()
    private var previousLoadingDisposable: Disposable? = null
    private var hasPreviousMessages = true

    val configuration = UsedeskChatSdk.requireConfiguration()
    var groupAgentMessages: Boolean = true

    init {
        setModel { copy(messageDraft = usedeskChat.getMessageDraft()) }

        actionListenerRx = object : IUsedeskActionListenerRx() {
            override fun onMessagesObservable(
                messagesObservable: Observable<List<UsedeskMessage>>
            ) = messagesObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
                val lastBottomMessage = messages.lastOrNull()
                val bottomMessageIndex = it.indexOfLast { message ->
                    message.id == lastBottomMessage?.id
                }
                messages = it
                setModel {
                    copy(
                        chatItems = getChatItems(),
                        newMessagesCount = newMessagesCount + when {
                            bottomMessageIndex < it.size - 1 -> it.size - 1 - bottomMessageIndex
                            else -> 0
                        }
                    )
                }
            }
        }
        usedeskChat.addActionListener(actionListenerRx)
    }

    private fun convertMessages(messages: List<UsedeskMessage>): List<ChatItem> {
        val newMessages = messages.reversed().groupBy {
            it.createdAt[Calendar.YEAR] * 1000 + it.createdAt[Calendar.DAY_OF_YEAR]
        }.flatMap {
            it.value.mapIndexed { i, message ->
                val lastOfGroup = i == 0
                if (message is UsedeskMessageClient) {
                    ClientMessage(message, lastOfGroup)
                } else {
                    AgentMessage(message, lastOfGroup, showName = true, showAvatar = true)
                }
            }.asSequence() + ChatDate((it.value.first().createdAt.clone() as Calendar).apply {
                set(Calendar.MILLISECOND, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.HOUR, 0)
            })
        }.toList()

        return if (groupAgentMessages) {
            newMessages.flatMapIndexed { index, item ->
                if (item is AgentMessage) {
                    item.message as UsedeskMessageAgent
                    val previous = (newMessages.getOrNull(index - 1) as? AgentMessage)?.message
                            as? UsedeskMessageAgent
                    val next = (newMessages.getOrNull(index + 1) as? AgentMessage)?.message
                            as? UsedeskMessageAgent
                    val newItem = AgentMessage(
                        item.message,
                        item.isLastOfGroup,
                        showName = false,
                        showAvatar = previous?.isAgentsTheSame(item.message) != true
                    )
                    if (next?.isAgentsTheSame(item.message) != true) {
                        sequenceOf(newItem, MessageAgentName(item.message.name))
                    } else {
                        sequenceOf(newItem)
                    }
                } else {
                    sequenceOf(item)
                }
            }
        } else {
            newMessages
        }
    }

    private fun UsedeskMessageAgent.isAgentsTheSame(other: UsedeskMessageAgent): Boolean {
        return avatar == other.avatar && name == other.name
    }

    fun onMessageChanged(message: String) {
        if (message != modelLiveData.value.messageDraft.text) {
            setModel { copy(messageDraft = messageDraft.copy(text = message)) }
            doIt(usedeskChat.setMessageDraftRx(modelLiveData.value.messageDraft))
        }
    }

    fun sendFeedback(message: UsedeskMessageAgentText, feedback: UsedeskFeedback) {
        doIt(usedeskChat.sendRx(message, feedback))
    }

    fun attachFiles(uriList: Set<UsedeskFileInfo>) {
        if (uriList != modelLiveData.value.messageDraft.files) {
            setModel {
                val newFiles = (messageDraft.files + uriList).toSet().toList()
                copy(
                    messageDraft = messageDraft.copy(files = newFiles),
                    attachmentPanelVisible = false
                )
            }
            doIt(usedeskChat.setMessageDraftRx(modelLiveData.value.messageDraft))
        }
    }

    fun detachFile(file: UsedeskFileInfo) {
        setModel { copy(messageDraft = messageDraft.copy(files = messageDraft.files - file)) }
        doIt(usedeskChat.setMessageDraftRx(modelLiveData.value.messageDraft))
    }

    fun onSendButton(message: String) {
        usedeskChat.send(message)
    }

    fun onSend() {
        doIt(usedeskChat.sendMessageDraftRx())

        setModel { copy(messageDraft = UsedeskMessageDraft()) }
    }

    fun sendAgain(id: Long) {
        doIt(usedeskChat.sendAgainRx(id))
    }

    fun removeMessage(id: Long) {
        doIt(usedeskChat.removeMessageRx(id))
    }

    fun showToBottomButton(show: Boolean) {
        setModel { copy(fabToBottom = show) }
    }

    fun showAttachmentPanel(show: Boolean) {
        setModel { copy(attachmentPanelVisible = show) }
    }

    override fun onCleared() {
        super.onCleared()

        usedeskChat.removeActionListener(actionListenerRx)

        UsedeskChatSdk.release(false)
    }

    private var cameraFile: File? = null

    fun setCameraFile(cameraFile: File) {
        this.cameraFile = cameraFile
    }

    fun useCameraFile(onCameraFile: (File) -> Unit) {
        cameraFile?.let {
            cameraFile = null
            onCameraFile(it)
        }
    }

    private fun onLastMessageShowed() {
        if (previousLoadingDisposable == null && hasPreviousMessages) {
            setModel { copy(chatItems = getChatItems()) }
            previousLoadingDisposable = doIt(Single.create<Boolean> {
                try {
                    it.onSuccess(usedeskChat.loadPreviousMessagesPage())
                } catch (e: Exception) {
                    e.printStackTrace()
                    it.onSuccess(true)
                }
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()), {
                previousLoadingDisposable = null
                hasPreviousMessages = it
                setModel { copy(chatItems = getChatItems()) }
            })
        }
    }

    fun onMessagesShowed(messagesRange: IntRange) {
        val lastMessageIndex = chatItems.indices.indexOfLast { i ->
            i <= messagesRange.last && chatItems[i] is ChatMessage
        }
        if (lastMessageIndex + ITEMS_UNTIL_LAST >= chatItems.size) {
            onLastMessageShowed()
        }
        if (messagesRange.first < modelLiveData.value.newMessagesCount) {
            setModel { copy(newMessagesCount = messagesRange.first) }
        }
    }

    private fun getChatItems(): List<ChatItem> {
        val messages = convertMessages(messages)
        return if (hasPreviousMessages) {
            messages.toMutableList().apply {
                add(
                    if (lastOrNull() as? ChatDate != null) {
                        messages.size - 1
                    } else {
                        messages.size
                    }, ChatLoading
                )
            }
        } else {
            messages
        }.also {
            chatItems = it
        }
    }

    data class Model(
        val messageDraft: UsedeskMessageDraft = UsedeskMessageDraft(),
        val fabToBottom: Boolean = false,
        val chatItems: List<ChatItem> = listOf(),
        val messagesScroll: Long = 0L,
        val attachmentPanelVisible: Boolean = false,
        val newMessagesCount: Int = 0
    )

    internal sealed interface ChatItem

    sealed class ChatMessage(
        val message: UsedeskMessage,
        val isLastOfGroup: Boolean
    ) : ChatItem

    class ClientMessage(
        message: UsedeskMessage,
        isLastOfGroup: Boolean
    ) : ChatMessage(message, isLastOfGroup)

    class AgentMessage(
        message: UsedeskMessage,
        isLastOfGroup: Boolean,
        val showName: Boolean,
        val showAvatar: Boolean
    ) : ChatMessage(message, isLastOfGroup)

    class MessageAgentName(
        val name: String
    ) : ChatItem

    class ChatDate(
        val calendar: Calendar
    ) : ChatItem

    object ChatLoading : ChatItem

    companion object {
        private const val ITEMS_UNTIL_LAST = 5
    }
}