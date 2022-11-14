package ru.usedesk.chat_gui.chat.messages

import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.*
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgent
import ru.usedesk.chat_sdk.entity.UsedeskMessageClient
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent
import java.util.*
import kotlin.math.min

internal class MessagesReducer(
    private val usedeskChat: IUsedeskChat,
    private val viewModel: MessagesViewModel
) {

    fun reduceModel(state: State, event: Event): State = state.reduce(event)

    private fun State.reduce(event: Event) = when (event) {
        is Event.Init -> init(event)
        is Event.Messages -> messages(event)
        is Event.MessageDraft -> messageDraft(event)
        is Event.MessagesShowed -> messagesShowed(event)
        is Event.PreviousMessagesResult -> previousMessagesResult(event)
        is Event.MessageChanged -> messageChanged(event)
        is Event.SendFeedback -> sendFeedback(event)
        is Event.AttachFiles -> attachFiles(event)
        is Event.DetachFile -> detachFile(event)
        is Event.ButtonSend -> buttonSend(event)
        is Event.SendAgain -> sendAgain(event)
        is Event.RemoveMessage -> removeMessage(event)
        is Event.ShowToBottomButton -> showToBottomButton(event)
        is Event.ShowAttachmentPanel -> showAttachmentPanel(event)
        Event.SendDraft -> sendDraft()
        is Event.AgentItemChanged -> agentItemChanged(event)
    }

    private fun State.agentItemChanged(event: Event.AgentItemChanged) = copy(
        agentItems = agentItems.map {
            when (it.item.id) {
                event.agentItem.item.id -> event.agentItem
                else -> it
            }
        }
    )

    private fun State.showToBottomButton(event: Event.ShowToBottomButton) =
        copy(fabToBottom = event.show)

    private fun State.showAttachmentPanel(event: Event.ShowAttachmentPanel) =
        copy(attachmentPanelVisible = event.show)

    private fun State.removeMessage(event: Event.RemoveMessage) = this.apply {
        viewModel.doIo { usedeskChat.removeMessage(event.id) }
    }

    private fun State.sendAgain(event: Event.SendAgain) = this.apply {
        viewModel.doIo { usedeskChat.sendAgain(event.id) }
    }

    private fun State.sendDraft() = copy(
        messageDraft = UsedeskMessageDraft(),
        goToBottom = UsedeskSingleLifeEvent(Unit)
    ).apply {
        viewModel.doIo { usedeskChat.sendMessageDraft() }
    }

    private fun State.buttonSend(event: Event.ButtonSend) = copy(
        goToBottom = UsedeskSingleLifeEvent(Unit)
    ).apply {
        viewModel.doIo { usedeskChat.send(event.message) }
    }

    private fun State.attachFiles(event: Event.AttachFiles) = copy(
        messageDraft = messageDraft.copy(
            files = (messageDraft.files + event.files).toSet().toList()
        ),
        attachmentPanelVisible = false
    ).apply {
        viewModel.doIo { usedeskChat.setMessageDraft(messageDraft) }
    }


    private fun State.detachFile(event: Event.DetachFile) = copy(
        messageDraft = messageDraft.copy(files = messageDraft.files - event.file)
    ).apply {
        viewModel.doIo { usedeskChat.setMessageDraft(messageDraft) }
    }

    private fun State.sendFeedback(event: Event.SendFeedback) = this.apply {
        viewModel.doIo {
            usedeskChat.send(
                event.message,
                event.feedback
            )
        }
    }

    private fun State.messageChanged(event: Event.MessageChanged): State = when (event.message) {
        messageDraft.text -> this
        else -> copy(messageDraft = messageDraft.copy(text = event.message)).apply {
            viewModel.doIo { usedeskChat.setMessageDraft(messageDraft) }
        }
    }

    private fun State.init(event: Event.Init) =
        copy(groupAgentMessages = event.groupAgentMessages)

    private fun State.previousMessagesResult(event: Event.PreviousMessagesResult) = copy(
        hasPreviousMessages = event.hasPreviousMessages,
        previousLoading = false,
        chatItems = when (this.hasPreviousMessages) {
            event.hasPreviousMessages -> chatItems
            else -> messages.convert(
                event.hasPreviousMessages,
                groupAgentMessages
            )
        }
    )

    private fun State.messagesShowed(event: Event.MessagesShowed): State {
        val lastMessageIndex = chatItems.indices.indexOfLast { i ->
            i <= event.messagesRange.last && chatItems[i] is ChatItem.Message
        }
        var previousLoading = this.previousLoading
        if (lastMessageIndex + ITEMS_UNTIL_LAST >= chatItems.size &&
            !previousLoading
            && hasPreviousMessages
        ) {
            previousLoading = true
            ioIntent {
                val hasPreviousMessages = try {
                    usedeskChat.loadPreviousMessagesPage()
                } catch (e: Exception) {
                    e.printStackTrace()
                    true
                }
                Event.PreviousMessagesResult(hasPreviousMessages)
            }
        }
        val agentMessageShowed = event.messagesRange
            .asSequence()
            .map(chatItems::getOrNull)
            .firstOrNull { it is ChatItem.Message.Agent }
        val newAgentIndexShowed = when (agentMessageShowed) {
            null -> agentIndexShowed
            else -> min(agentIndexShowed, agentMessages.indexOf(agentMessageShowed))
        }
        return copy(
            previousLoading = previousLoading,
            fabToBottom = event.messagesRange.first > 0,
            chatItems = when (this.previousLoading) {
                previousLoading -> chatItems
                else -> messages.convert(
                    hasPreviousMessages,
                    groupAgentMessages
                )
            },
            agentIndexShowed = newAgentIndexShowed
        )
    }

    private fun State.messageDraft(event: Event.MessageDraft) = copy(
        messageDraft = event.messageDraft
    )

    private fun State.getNewAgentIndexShowed(newAgentItems: List<ChatItem.Message.Agent>): Int =
        when (val lastMessage = agentMessages.getOrNull(agentIndexShowed)) {
            null -> 0
            else -> newAgentItems.indexOfFirst { it.message.id == lastMessage.message.id }
        }

    private fun State.messages(event: Event.Messages): State {
        val newChatItems = event.messages.convert(
            hasPreviousMessages,
            groupAgentMessages
        )
        val newAgentItems = newChatItems.filterIsInstance<ChatItem.Message.Agent>()
        val newAgentMessageShowed = getNewAgentIndexShowed(newAgentItems)
        return copy(
            messages = event.messages,
            agentMessages = newAgentItems,
            chatItems = newChatItems,
            agentIndexShowed = newAgentMessageShowed
        )
    }

    private fun List<UsedeskMessage>.convert(
        hasPreviousMessages: Boolean,
        groupAgentMessages: Boolean
    ): List<ChatItem> {
        val newMessages = reversed()
            .groupBy { it.createdAt[Calendar.YEAR] * 1000 + it.createdAt[Calendar.DAY_OF_YEAR] }
            .flatMap {
                it.value.mapIndexed { i, message ->
                    val lastOfGroup = i == 0
                    when (message) {
                        is UsedeskMessageClient -> ChatItem.Message.Client(message, lastOfGroup)
                        else -> ChatItem.Message.Agent(
                            message,
                            lastOfGroup,
                            showName = true,
                            showAvatar = true
                        )
                    }
                }.asSequence() + ChatItem.ChatDate(
                    (it.value.first().createdAt.clone() as Calendar).apply {
                        set(Calendar.MILLISECOND, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.HOUR, 0)
                    })
            }.toList()

        val messages = when {
            groupAgentMessages -> newMessages.flatMapIndexed { index, item ->
                when (item) {
                    is ChatItem.Message.Agent -> {
                        item.message as UsedeskMessageAgent
                        val previous = (newMessages.getOrNull(index - 1)
                                as? ChatItem.Message.Agent)?.message
                                as? UsedeskMessageAgent
                        val next = (newMessages.getOrNull(index + 1)
                                as? ChatItem.Message.Agent)?.message
                                as? UsedeskMessageAgent
                        val newItem = ChatItem.Message.Agent(
                            item.message,
                            item.isLastOfGroup,
                            showName = false,
                            showAvatar = previous?.isAgentsTheSame(item.message) != true
                        )
                        when (next?.isAgentsTheSame(item.message)) {
                            true -> sequenceOf(newItem)
                            else -> sequenceOf(
                                newItem,
                                ChatItem.MessageAgentName(item.message.name)
                            )
                        }
                    }
                    else -> sequenceOf(item)
                }
            }
            else -> newMessages
        }
        return when {
            hasPreviousMessages -> messages.toMutableList().apply {
                add(
                    when (lastOrNull() as? ChatItem.ChatDate) {
                        null -> messages.size
                        else -> messages.size - 1
                    },
                    ChatItem.Loading
                )
            }
            else -> messages
        }
    }

    private fun UsedeskMessageAgent.isAgentsTheSame(other: UsedeskMessageAgent): Boolean =
        avatar == other.avatar && name == other.name

    private fun ioIntent(getEvent: suspend () -> Event) {
        viewModel.doIo {
            val intent = getEvent()
            viewModel.doMain { viewModel.onIntent(intent) }
        }
    }

    companion object {
        private const val ITEMS_UNTIL_LAST = 5
    }
}