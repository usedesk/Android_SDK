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

    fun reduceModel(model: Model, intent: Intent): Model = model.reduce(intent)

    private fun Model.reduce(intent: Intent) = when (intent) {
        is Intent.Init -> init(intent)
        is Intent.Messages -> messages(intent)
        is Intent.MessageDraft -> messageDraft(intent)
        is Intent.MessagesShowed -> messagesShowed(intent)
        is Intent.PreviousMessagesResult -> previousMessagesResult(intent)
        is Intent.MessageChanged -> messageChanged(intent)
        is Intent.SendFeedback -> sendFeedback(intent)
        is Intent.AttachFiles -> attachFiles(intent)
        is Intent.DetachFile -> detachFile(intent)
        is Intent.ButtonSend -> buttonSend(intent)
        is Intent.SendAgain -> sendAgain(intent)
        is Intent.RemoveMessage -> removeMessage(intent)
        is Intent.ShowToBottomButton -> showToBottomButton(intent)
        is Intent.ShowAttachmentPanel -> showAttachmentPanel(intent)
        Intent.SendDraft -> sendDraft()
    }

    private fun Model.showToBottomButton(intent: Intent.ShowToBottomButton) =
        copy(fabToBottom = intent.show)

    private fun Model.showAttachmentPanel(intent: Intent.ShowAttachmentPanel) =
        copy(attachmentPanelVisible = intent.show)

    private fun Model.removeMessage(intent: Intent.RemoveMessage) = this.apply {
        viewModel.doIo { usedeskChat.removeMessage(intent.id) }
    }

    private fun Model.sendAgain(intent: Intent.SendAgain) = this.apply {
        viewModel.doIo { usedeskChat.sendAgain(intent.id) }
    }

    private fun Model.sendDraft() = copy(
        messageDraft = UsedeskMessageDraft(),
        goToBottom = UsedeskSingleLifeEvent(Unit)
    ).apply {
        viewModel.doIo { usedeskChat.sendMessageDraft() }
    }

    private fun Model.buttonSend(intent: Intent.ButtonSend) = copy(
        goToBottom = UsedeskSingleLifeEvent(Unit)
    ).apply {
        viewModel.doIo { usedeskChat.send(intent.message) }
    }

    private fun Model.attachFiles(intent: Intent.AttachFiles) = copy(
        messageDraft = messageDraft.copy(
            files = (messageDraft.files + intent.files).toSet().toList()
        ),
        attachmentPanelVisible = false
    ).apply {
        viewModel.doIo { usedeskChat.setMessageDraft(messageDraft) }
    }


    private fun Model.detachFile(intent: Intent.DetachFile) = copy(
        messageDraft = messageDraft.copy(files = messageDraft.files - intent.file)
    ).apply {
        viewModel.doIo { usedeskChat.setMessageDraft(messageDraft) }
    }

    private fun Model.sendFeedback(intent: Intent.SendFeedback) = this.apply {
        viewModel.doIo {
            usedeskChat.send(
                intent.message,
                intent.feedback
            )
        }
    }

    private fun Model.messageChanged(intent: Intent.MessageChanged): Model = when (intent.message) {
        messageDraft.text -> this
        else -> copy(messageDraft = messageDraft.copy(text = intent.message)).apply {
            viewModel.doIo { usedeskChat.setMessageDraft(messageDraft) }
        }
    }

    private fun Model.init(intent: Intent.Init) =
        copy(groupAgentMessages = intent.groupAgentMessages)

    private fun Model.previousMessagesResult(intent: Intent.PreviousMessagesResult) = copy(
        hasPreviousMessages = intent.hasPreviousMessages,
        previousLoading = false,
        chatItems = when (this.hasPreviousMessages) {
            intent.hasPreviousMessages -> chatItems
            else -> messages.convert(
                intent.hasPreviousMessages,
                groupAgentMessages
            )
        }
    )

    private fun Model.messagesShowed(intent: Intent.MessagesShowed): Model {
        val lastMessageIndex = chatItems.indices.indexOfLast { i ->
            i <= intent.messagesRange.last && chatItems[i] is ChatItem.Message
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
                Intent.PreviousMessagesResult(hasPreviousMessages)
            }
        }
        val curAgentItemShowed = intent.messagesRange
            .asSequence()
            .map(chatItems::getOrNull)
            .firstOrNull { it is ChatItem.Message.Agent }
        val newAgentIndexShowed = when (curAgentItemShowed) {
            null -> agentIndexShowed
            else -> min(agentIndexShowed, agentItems.indexOf(curAgentItemShowed))
        }
        return copy(
            previousLoading = previousLoading,
            fabToBottom = intent.messagesRange.first > 0,
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

    private fun Model.messageDraft(intent: Intent.MessageDraft) = copy(
        messageDraft = intent.messageDraft
    )

    private fun Model.getNewAgentIndexShowed(newAgentItems: List<ChatItem.Message.Agent>): Int =
        when (val lastMessage = agentItems.getOrNull(agentIndexShowed)) {
            null -> 0
            else -> newAgentItems.indexOfFirst { it.message.id == lastMessage.message.id }
        }

    private fun Model.messages(intent: Intent.Messages): Model {
        val newChatItems = intent.messages.convert(
            hasPreviousMessages,
            groupAgentMessages
        )
        val newAgentItems = newChatItems.filterIsInstance<ChatItem.Message.Agent>()
        val newAgentMessageShowed = getNewAgentIndexShowed(newAgentItems)
        return copy(
            messages = intent.messages,
            agentItems = newAgentItems,
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

    private fun ioIntent(getIntent: suspend () -> Intent) {
        viewModel.doIo {
            val intent = getIntent()
            viewModel.doMain { viewModel.onIntent(intent) }
        }
    }

    companion object {
        private const val ITEMS_UNTIL_LAST = 5
    }
}