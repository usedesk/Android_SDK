package ru.usedesk.chat_gui.chat.messages

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_gui.UsedeskViewModel
import java.io.File
import java.util.*

internal class MessagesViewModel : UsedeskViewModel<MessagesViewModel.Model>(Model()) {

    private val actionListenerRx: IUsedeskActionListenerRx
    private val usedeskChat: IUsedeskChat = UsedeskChatSdk.requireInstance()

    private var messages: List<UsedeskMessage> = listOf()

    val configuration = UsedeskChatSdk.requireConfiguration()
    var groupAgentMessages: Boolean = true

    init {
        setModel { model ->
            model.copy(messageDraft = usedeskChat.getMessageDraft())
        }

        actionListenerRx = object : IUsedeskActionListenerRx() {
            override fun onMessagesObservable(
                messagesObservable: Observable<List<UsedeskMessage>>
            ): Disposable? {
                return messagesObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
                    messages = it
                    setModel { model ->
                        model.copy(chatItems = convertMessages(messages))
                    }
                }
            }
        }
        usedeskChat.addActionListener(actionListenerRx)
    }

    private fun convertMessages(messages: List<UsedeskMessage>): List<ChatItem> {
        val newMessages = messages.groupBy {
            it.createdAt[Calendar.YEAR] * 1000 + it.createdAt[Calendar.DAY_OF_YEAR]
        }.flatMap {
            sequenceOf(ChatDate(it.value.first().createdAt)) + it.value.mapIndexed { i, message ->
                val lastOfGroup = it.value.size - 1 == i
                if (message is UsedeskMessageClient) {
                    ClientMessage(message, lastOfGroup)
                } else {
                    AgentMessage(message, lastOfGroup, showName = true, showAvatar = true)
                }
            }
        }.toMutableList()

        if (groupAgentMessages) {
            newMessages.forEachIndexed { index, item ->
                if (item is AgentMessage) {
                    item.message as UsedeskMessageAgent
                    val previous = (newMessages.getOrNull(index - 1) as? AgentMessage)?.message
                            as? UsedeskMessageAgent
                    val next = (newMessages.getOrNull(index + 1) as? AgentMessage)?.message
                            as? UsedeskMessageAgent
                    newMessages[index] = AgentMessage(
                        item.message,
                        item.isLastOfGroup,
                        showName = previous?.isAgentsTheSame(item.message) != true,
                        showAvatar = next?.isAgentsTheSame(item.message) != true
                    )
                }
            }
        }

        return newMessages
    }

    private fun UsedeskMessageAgent.isAgentsTheSame(other: UsedeskMessageAgent): Boolean {
        return avatar == other.avatar && name == other.name
    }

    fun onMessageChanged(message: String) {
        if (message != modelLiveData.value.messageDraft.text) {
            setModel { model ->
                model.copy(messageDraft = model.messageDraft.copy(text = message))
            }
            doIt(usedeskChat.setMessageDraftRx(modelLiveData.value.messageDraft))
        }
    }

    fun sendFeedback(message: UsedeskMessageAgentText, feedback: UsedeskFeedback) {
        doIt(usedeskChat.sendRx(message, feedback))
    }

    fun attachFiles(uriList: Set<UsedeskFileInfo>) {
        if (uriList != modelLiveData.value.messageDraft.files) {
            setModel { model ->
                val newFiles = (model.messageDraft.files + uriList).toSet().toList()
                model.copy(
                    messageDraft = model.messageDraft.copy(files = newFiles),
                    attachmentPanelVisible = false
                )
            }
            doIt(usedeskChat.setMessageDraftRx(modelLiveData.value.messageDraft))
        }
    }

    fun detachFile(file: UsedeskFileInfo) {
        setModel { model ->
            model.copy(
                messageDraft = model.messageDraft.copy(
                    files = model.messageDraft.files - file
                )
            )
        }
        doIt(usedeskChat.setMessageDraftRx(modelLiveData.value.messageDraft))
    }

    fun onSendButton(message: String) {
        doIt(usedeskChat.sendRx(message))
    }

    fun onSend() {
        doIt(usedeskChat.sendMessageDraftRx())

        setModel { model ->
            model.copy(messageDraft = UsedeskMessageDraft())
        }
    }

    fun sendAgain(id: Long) {
        doIt(usedeskChat.sendAgainRx(id))
    }

    fun removeMessage(id: Long) {
        doIt(usedeskChat.removeMessageRx(id))
    }

    fun showToBottomButton(show: Boolean) {
        setModel { model ->
            model.copy(fabToBottom = show)
        }
    }

    fun showAttachmentPanel(show: Boolean) {
        setModel { model ->
            model.copy(
                attachmentPanelVisible = show
            )
        }
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

    private var previousLoadingDisposable: Disposable? = null

    fun onMessageShowed(firstMessageIndex: Int) {
        if (firstMessageIndex <= 5 &&
            previousLoadingDisposable == null
        ) {
            setModel { model ->
                model.copy(loading = true)
            }
            previousLoadingDisposable = doIt(usedeskChat.loadPreviousMessagesPageRx(), {
                previousLoadingDisposable = null
                setModel { model ->
                    model.copy(loading = false)
                }
            }, {
                previousLoadingDisposable = null
                setModel { model ->
                    model.copy(loading = false)
                }
            })
        }
    }

    data class Model(
        val messageDraft: UsedeskMessageDraft = UsedeskMessageDraft(),
        val fabToBottom: Boolean = false,
        val chatItems: List<ChatItem> = listOf(),
        val messagesScroll: Long = 0L,
        val attachmentPanelVisible: Boolean = false,
        val loading: Boolean = false
    )

    internal sealed class ChatItem

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

    open class ChatMessage(
        val message: UsedeskMessage,
        val isLastOfGroup: Boolean
    ) : ChatItem()

    class ChatDate(
        val calendar: Calendar
    ) : ChatItem()
}