package ru.usedesk.chat_gui.chat.messages

import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Button
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Field
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import java.util.*

internal class MessagesViewModel : UsedeskViewModel<MessagesViewModel.State>(State()) {

    private val actionListener: IUsedeskActionListener
    private val usedeskChat = UsedeskChatSdk.requireInstance()

    private val messagesReducer = MessagesReducer(usedeskChat)

    fun onEvent(event: Event) {
        setModel { messagesReducer.reduceModel(this, event) }
    }

    init {
        onEvent(Event.MessageDraft(usedeskChat.getMessageDraft()))

        actionListener = object : IUsedeskActionListener {
            override fun onModel(
                model: IUsedeskChat.Model,
                newMessages: List<UsedeskMessage>,
                updatedMessages: List<UsedeskMessage>,
                removedMessages: List<UsedeskMessage>
            ) {
                doMain {
                    onEvent(Event.ChatModel(model))
                }
            }
        }
        usedeskChat.addActionListener(actionListener)
    }

    override fun onCleared() {
        super.onCleared()

        usedeskChat.removeActionListener(actionListener)

        UsedeskChatSdk.release(false)
    }

    sealed interface Event {
        class Init(val groupAgentMessages: Boolean) : Event
        class ChatModel(val model: IUsedeskChat.Model) : Event
        class MessageDraft(val messageDraft: UsedeskMessageDraft) : Event
        class MessagesShowed(val messagesRange: IntRange) : Event
        class MessageChanged(val message: String) : Event

        //class PreviousMessagesResult(val hasPreviousMessages: Boolean) : Event
        class SendFeedback(
            val message: UsedeskMessageAgentText,
            val feedback: UsedeskFeedback
        ) : Event

        class AttachFiles(val files: Set<UsedeskFileInfo>) : Event
        class DetachFile(val file: UsedeskFileInfo) : Event
        class SendAgain(val id: Long) : Event
        class RemoveMessage(val id: Long) : Event
        class ShowToBottomButton(val show: Boolean) : Event
        class ShowAttachmentPanel(val show: Boolean) : Event
        object SendDraft : Event
        class MessageButtonClick(val button: Button) : Event
        class FormApplyClick(val messageId: Long) : Event
        class FormChanged(val messageId: Long, val field: Field) : Event
        class FormListClicked(val messageId: Long, val field: Field) : Event
    }

    data class State(
        val messages: List<UsedeskMessage> = listOf(),
        val formMap: Map<Long, UsedeskForm> = mapOf(),
        val agentMessages: List<ChatItem.Message.Agent> = listOf(),
        val messageDraft: UsedeskMessageDraft = UsedeskMessageDraft(),
        val fabToBottom: Boolean = false,
        val chatItems: List<ChatItem> = listOf(),
        val messagesScroll: Long = 0L,
        val attachmentPanelVisible: Boolean = false,
        val agentMessageShowed: Int = 0,
        val hasPreviousMessages: Boolean = true,
        val groupAgentMessages: Boolean = false,
        val previousLoading: Boolean = false,
        val goToBottom: UsedeskEvent<Unit>? = null,
        val openUrl: UsedeskEvent<String>? = null,
        val lastChatModel: IUsedeskChat.Model? = null
    )

    internal sealed interface ChatItem {
        sealed interface Message : ChatItem {
            val message: UsedeskMessage
            val isLastOfGroup: Boolean

            data class Client(
                override val message: UsedeskMessage,
                override val isLastOfGroup: Boolean
            ) : Message

            data class Agent(
                override val message: UsedeskMessage,
                override val isLastOfGroup: Boolean,
                val showName: Boolean,
                val showAvatar: Boolean,
                val form: UsedeskForm?
            ) : Message
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