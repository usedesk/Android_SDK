package ru.usedesk.chat_gui.chat.messages

import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Item
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.common_sdk.entity.UsedeskEvent
import java.util.*

internal class MessagesViewModel : UsedeskViewModel<MessagesViewModel.State>(State()) {

    private val actionListener: IUsedeskActionListener
    private val usedeskChat = UsedeskChatSdk.requireInstance()

    private val messagesReducer = MessagesReducer(usedeskChat, this)

    fun onIntent(event: Event) {
        setModel { messagesReducer.reduceModel(this, event) }
    }

    init {
        onIntent(Event.MessageDraft(usedeskChat.getMessageDraft()))

        actionListener = object : IUsedeskActionListener {
            override fun onMessagesReceived(messages: List<UsedeskMessage>) {
                doMain { onIntent(Event.Messages(messages)) }
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
        class AgentItemChanged(val agentItem: AgentItem<*, *>) : Event
    }

    data class State(
        val messages: List<UsedeskMessage> = listOf(),
        val agentMessages: List<ChatItem.Message.Agent> = listOf(),
        val agentItems: List<AgentItem<*, *>> = listOf(),
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

    internal sealed interface AgentItem<ITEM : Item, STATE : ItemState> {
        val item: ITEM
        val state: STATE

        data class Text(
            override val item: Item.Field.Text,
            override val state: ItemState.Text
        ) : AgentItem<Item.Field.Text, ItemState.Text>

        data class CheckBox(
            override val item: Item.Field.CheckBox,
            override val state: ItemState.CheckBox
        ) : AgentItem<Item.Field.CheckBox, ItemState.CheckBox>

        data class ItemList(
            override val item: Item.Field.ItemList,
            override val state: ItemState.ItemList
        ) : AgentItem<Item.Field.ItemList, ItemState.ItemList>
    }

    internal sealed interface ItemState {
        data class Button(val enabled: Boolean = false) : ItemState
        data class CheckBox(val checked: Boolean = false) : ItemState
        data class ItemList(val selected: List<Item.Field.ItemList.ListItem> = listOf()) : ItemState
        data class Text(
            val text: String = "",
            val focused: Boolean = false
        ) : ItemState
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