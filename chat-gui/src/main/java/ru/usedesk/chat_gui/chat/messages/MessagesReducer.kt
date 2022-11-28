package ru.usedesk.chat_gui.chat.messages

import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.*
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft
import ru.usedesk.chat_sdk.entity.UsedeskMessageOwner
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
        is Event.FormChanged -> formChanged(event)
        is Event.FormApplyClick -> formApplyClick(event)
        is Event.FormListClicked -> formListClicked(event)
        Event.SendDraft -> sendDraft()
    }

    private fun State.formApplyClick(event: Event.FormApplyClick) = copy()

    private fun State.formListClicked(event: Event.FormListClicked) = copy()

    private fun State.formChanged(event: Event.FormChanged) = copy(
        agentItemsState = agentItemsState.toMutableMap().apply {
            put(event.form.id, event.formState)
        }
    )

    private fun State.showToBottomButton(event: Event.ShowToBottomButton) =
        copy(fabToBottom = event.show)

    private fun State.showAttachmentPanel(event: Event.ShowAttachmentPanel) =
        copy(attachmentPanelVisible = event.show)

    private fun State.removeMessage(event: Event.RemoveMessage) = this.apply {
        usedeskChat.removeMessage(event.id)
    }

    private fun State.sendAgain(event: Event.SendAgain) = this.apply {
        usedeskChat.sendAgain(event.id)
    }

    private fun State.sendDraft() = copy(
        messageDraft = UsedeskMessageDraft(),
        goToBottom = UsedeskSingleLifeEvent(Unit)
    ).apply {
        usedeskChat.sendMessageDraft()
    }

    private fun State.buttonSend(event: Event.ButtonSend) = copy(
        goToBottom = UsedeskSingleLifeEvent(Unit)
    ).apply {
        usedeskChat.send(event.message)
    }

    private fun State.attachFiles(event: Event.AttachFiles) = copy(
        messageDraft = messageDraft.copy(
            files = (messageDraft.files + event.files).toSet().toList()
        ),
        attachmentPanelVisible = false
    ).apply {
        usedeskChat.setMessageDraft(messageDraft)
    }


    private fun State.detachFile(event: Event.DetachFile) = copy(
        messageDraft = messageDraft.copy(files = messageDraft.files - event.file)
    ).apply {
        usedeskChat.setMessageDraft(messageDraft)
    }

    private fun State.sendFeedback(event: Event.SendFeedback) = apply {
        usedeskChat.send(
            event.message,
            event.feedback
        )
    }

    private fun State.messageChanged(event: Event.MessageChanged): State = when (event.message) {
        messageDraft.text -> this
        else -> copy(messageDraft = messageDraft.copy(text = event.message)).apply {
            usedeskChat.setMessageDraft(messageDraft)
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
            usedeskChat.loadPreviousMessagesPage()
        }
        val agentMessages = event.messagesRange
            .map { chatItems.getOrNull(it) }
        agentMessages.forEach {
            if (it is ChatItem.Message &&
                it.message is UsedeskMessageAgentText &&
                it.message.formsLoaded
            ) {
                usedeskChat.loadForm(it.message.id)
            }
        }
        val agentMessageShowed = agentMessages
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
        val newAgentMessages = newChatItems.filterIsInstance<ChatItem.Message.Agent>()
        val newAgentMessageShowed = getNewAgentIndexShowed(newAgentMessages)
        return copy(
            messages = event.messages,
            agentMessages = newAgentMessages,
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
                        is UsedeskMessageOwner.Client -> ChatItem.Message.Client(
                            message,
                            lastOfGroup
                        )
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
                        item.message as UsedeskMessageOwner.Agent
                        val previous = (newMessages.getOrNull(index - 1)
                                as? ChatItem.Message.Agent)?.message
                                as? UsedeskMessageOwner.Agent
                        val next = (newMessages.getOrNull(index + 1)
                                as? ChatItem.Message.Agent)?.message
                                as? UsedeskMessageOwner.Agent
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

    private fun UsedeskMessageOwner.Agent.isAgentsTheSame(other: UsedeskMessageOwner.Agent): Boolean =
        avatar == other.avatar && name == other.name

    /*private fun ioEvent(getEvent: suspend () -> Event) {
        viewModel.doIo {
            val intent = getEvent()
            viewModel.doMain { viewModel.onEvent(intent) }
        }
    }*/

    companion object {
        private const val ITEMS_UNTIL_LAST = 5
    }
}