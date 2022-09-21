package ru.usedesk.chat_gui.chat.messages

import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Intent
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Model
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
        viewModel.doIo { usedeskChat.removeMessageRx(intent.id) }
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
            i <= intent.messagesRange.last && chatItems[i] is MessagesViewModel.ChatMessage
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
            lastMessageShowed = min(lastMessageShowed, intent.messagesRange.first)
        )
    }

    private fun Model.messageDraft(intent: Intent.MessageDraft) = copy(
        messageDraft = intent.messageDraft
    )

    private fun Model.messages(intent: Intent.Messages): Model {
        val newMessages = intent.messages
        val lastMessage = messages.getOrNull(messages.size - lastMessageShowed - 1)
        return copy(
            messages = newMessages,
            chatItems = newMessages.convert(
                hasPreviousMessages,
                groupAgentMessages
            ),
            lastMessageShowed = when (lastMessage) {
                null -> 0
                else -> newMessages.size - 1 - newMessages.indexOfLast {
                    when (lastMessage) {
                        is UsedeskMessageClient -> it is UsedeskMessageClient &&
                                lastMessage.localId == it.localId
                        else -> lastMessage.id == it.id
                    }
                }
            }
        )
    }

    private fun List<UsedeskMessage>.convert(
        hasPreviousMessages: Boolean,
        groupAgentMessages: Boolean
    ): List<MessagesViewModel.ChatItem> {
        val newMessages = this.reversed().groupBy {
            it.createdAt[Calendar.YEAR] * 1000 + it.createdAt[Calendar.DAY_OF_YEAR]
        }.flatMap {
            it.value.mapIndexed { i, message ->
                val lastOfGroup = i == 0
                when (message) {
                    is UsedeskMessageClient -> MessagesViewModel.ClientMessage(message, lastOfGroup)
                    else -> MessagesViewModel.AgentMessage(
                        message,
                        lastOfGroup,
                        showName = true,
                        showAvatar = true
                    )
                }
            }.asSequence() + MessagesViewModel.ChatDate(
                (it.value.first().createdAt.clone() as Calendar).apply {
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.HOUR, 0)
                })
        }.toList()

        val messages = if (groupAgentMessages) {
            newMessages.flatMapIndexed { index, item ->
                if (item is MessagesViewModel.AgentMessage) {
                    item.message as UsedeskMessageAgent
                    val previous =
                        (newMessages.getOrNull(index - 1) as? MessagesViewModel.AgentMessage)?.message
                                as? UsedeskMessageAgent
                    val next =
                        (newMessages.getOrNull(index + 1) as? MessagesViewModel.AgentMessage)?.message
                                as? UsedeskMessageAgent
                    val newItem = MessagesViewModel.AgentMessage(
                        item.message,
                        item.isLastOfGroup,
                        showName = false,
                        showAvatar = previous?.isAgentsTheSame(item.message) != true
                    )
                    when (next?.isAgentsTheSame(item.message)) {
                        true -> sequenceOf(newItem)
                        else -> sequenceOf(
                            newItem,
                            MessagesViewModel.MessageAgentName(item.message.name)
                        )
                    }
                } else {
                    sequenceOf(item)
                }
            }
        } else {
            newMessages
        }
        return when {
            hasPreviousMessages -> messages.toMutableList().apply {
                add(
                    when (lastOrNull() as? MessagesViewModel.ChatDate) {
                        null -> messages.size
                        else -> messages.size - 1
                    },
                    MessagesViewModel.ChatLoading
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