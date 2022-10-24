package ru.usedesk.chat_gui.chat.messages

import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import java.util.*

internal class MessagesViewModel : UsedeskViewModel<MessagesViewModel.Model>(Model()) {

    private val actionListener: IUsedeskActionListener
    private val usedeskChat = UsedeskChatSdk.requireInstance()

    private val messagesReducer = MessagesReducer(usedeskChat, this)

    fun onIntent(intent: Intent) {
        setModel { messagesReducer.reduceModel(this, intent) }
    }

    init {
        onIntent(Intent.MessageDraft(usedeskChat.getMessageDraft()))

        actionListener = object : IUsedeskActionListener {
            override fun onMessagesReceived(messages: List<UsedeskMessage>) {
                doMain { onIntent(Intent.Messages(messages)) }
            }
        }
        usedeskChat.addActionListener(actionListener)
    }

    override fun onCleared() {
        super.onCleared()

        usedeskChat.removeActionListener(actionListener)

        UsedeskChatSdk.release(false)
    }

    sealed interface Intent {
        class Init(val groupAgentMessages: Boolean) : Intent
        class Messages(val messages: List<UsedeskMessage>) : Intent
        class MessageDraft(val messageDraft: UsedeskMessageDraft) : Intent
        class MessagesShowed(val messagesRange: IntRange) : Intent
        class MessageChanged(val message: String) : Intent
        class PreviousMessagesResult(val hasPreviousMessages: Boolean) : Intent
        class SendFeedback(
            val message: UsedeskMessageAgentText,
            val feedback: UsedeskFeedback
        ) : Intent

        class AttachFiles(val files: Set<UsedeskFileInfo>) : Intent
        class DetachFile(val file: UsedeskFileInfo) : Intent
        class ButtonSend(val message: String) : Intent
        class SendAgain(val id: Long) : Intent
        class RemoveMessage(val id: Long) : Intent
        class ShowToBottomButton(val show: Boolean) : Intent
        class ShowAttachmentPanel(val show: Boolean) : Intent
        object SendDraft : Intent
    }

    data class Model(
        val messages: List<UsedeskMessage> = listOf(),
        val agentItems: List<ChatItem.Message.Agent> = listOf(),
        val messageDraft: UsedeskMessageDraft = UsedeskMessageDraft(),
        val fabToBottom: Boolean = false,
        val chatItems: List<ChatItem> = listOf(),
        val messagesScroll: Long = 0L,
        val attachmentPanelVisible: Boolean = false,
        val agentIndexShowed: Int = 0,
        val hasPreviousMessages: Boolean = true,
        val groupAgentMessages: Boolean = false,
        val previousLoading: Boolean = false,
        val goToBottom: UsedeskEvent<Unit>? = null
    )

    internal sealed interface ChatItem {

        sealed class Message(
            val message: UsedeskMessage,
            val isLastOfGroup: Boolean
        ) : ChatItem {
            class Client(
                message: UsedeskMessage,
                isLastOfGroup: Boolean
            ) : Message(message, isLastOfGroup)

            class Agent(
                message: UsedeskMessage,
                isLastOfGroup: Boolean,
                val showName: Boolean,
                val showAvatar: Boolean
            ) : Message(message, isLastOfGroup)
        }

        class MessageAgentName(
            val name: String
        ) : ChatItem

        class ChatDate(
            val calendar: Calendar
        ) : ChatItem

        object Loading : ChatItem
    }
}