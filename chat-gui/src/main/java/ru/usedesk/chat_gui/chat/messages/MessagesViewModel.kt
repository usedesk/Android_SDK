package ru.usedesk.chat_gui.chat.messages

import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Form
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import java.util.*

internal class MessagesViewModel : UsedeskViewModel<MessagesViewModel.State>(State()) {

    private val actionListener: IUsedeskActionListener
    private val usedeskChat = UsedeskChatSdk.requireInstance()

    private val messagesReducer = MessagesReducer(usedeskChat, this)

    fun onEvent(event: Event) {
        setModel { messagesReducer.reduceModel(this, event) }
    }

    init {
        onEvent(Event.MessageDraft(usedeskChat.getMessageDraft()))

        actionListener = object : IUsedeskActionListener {
            override fun onMessagesReceived(messages: List<UsedeskMessage>) {
                doMain { onEvent(Event.Messages(messages)) }
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
        class Messages(val messages: List<UsedeskMessage>) : Event
        class MessageDraft(val messageDraft: UsedeskMessageDraft) : Event
        class MessagesShowed(val messagesRange: IntRange) : Event
        class MessageChanged(val message: String) : Event
        class PreviousMessagesResult(val hasPreviousMessages: Boolean) : Event
        class SendFeedback(
            val message: UsedeskMessageAgentText,
            val feedback: UsedeskFeedback
        ) : Event

        class AttachFiles(val files: Set<UsedeskFileInfo>) : Event
        class DetachFile(val file: UsedeskFileInfo) : Event
        class ButtonSend(val message: String) : Event
        class SendAgain(val id: Long) : Event
        class RemoveMessage(val id: Long) : Event
        class ShowToBottomButton(val show: Boolean) : Event
        class ShowAttachmentPanel(val show: Boolean) : Event
        object SendDraft : Event
        class FormApplyClick(val messageId: Long) : Event
        class FormChanged(
            val form: Form,
            val formItemState: FormItemState
        ) : Event

        class FormListClicked(
            val form: Form.Field.List,
            val formItemState: FormItemState.ItemList
        ) : Event
    }

    data class State(
        val messages: List<UsedeskMessage> = listOf(),
        val agentMessages: List<ChatItem.Message.Agent> = listOf(),
        val agentItemsState: Map<Long, FormItemState> = mapOf(),
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

    internal sealed interface FormItemState {
        data class Button(val enabled: Boolean = false) : FormItemState
        data class CheckBox(val checked: Boolean = false) : FormItemState
        data class ItemList(val selected: List<Form.Field.List.Item> = listOf()) : FormItemState
        data class Text(val text: String = "") : FormItemState
    }

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