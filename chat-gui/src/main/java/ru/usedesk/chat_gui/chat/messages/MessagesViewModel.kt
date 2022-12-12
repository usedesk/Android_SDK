package ru.usedesk.chat_gui.chat.messages

import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.domain.IUsedeskChat
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
        class ButtonSend(val message: String) : Event
        class SendAgain(val id: Long) : Event
        class RemoveMessage(val id: Long) : Event
        class ShowToBottomButton(val show: Boolean) : Event
        class ShowAttachmentPanel(val show: Boolean) : Event
        object SendDraft : Event
        class FormApplyClick(val messageId: Long) : Event
        class FormChanged(
            val messageId: Long,
            val form: Form,
            val formState: FormState
        ) : Event

        class FormListClicked(
            val form: Form.Field.List,
            val formState: FormState.List
        ) : Event
    }

    data class State(
        val messages: List<UsedeskMessage> = listOf(),
        val agentMessages: List<ChatItem.Message.Agent> = listOf(),
        val agentItemsState: Map<Long, Map<Long, FormState>> = mapOf(),
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
        val lastChatModel: IUsedeskChat.Model? = null
    )

    internal sealed interface FormState {
        data class Button(val enabled: Boolean = false) : FormState
        data class CheckBox(val checked: Boolean = false) : FormState
        data class List(val selected: kotlin.collections.List<Form.Field.List.Item> = listOf()) :
            FormState

        data class Text(val text: String = "") : FormState
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