
package ru.usedesk.chat_gui.chat.messages

import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.ChatItem
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.FormSelector
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.State
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.chat_sdk.entity.UsedeskForm.Field
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft
import ru.usedesk.chat_sdk.entity.UsedeskMessageOwner
import ru.usedesk.common_sdk.entity.UsedeskEvent
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.min

internal class MessagesReducer @Inject constructor(
    private val usedeskChat: IUsedeskChat
) {

    fun reduceModel(state: State, event: Event): State = state.reduce(event)

    private fun State.reduce(event: Event) = when (event) {
        is Event.Init -> init(event)
        is Event.ChatModel -> chatModel(event)
        is Event.MessageDraft -> messageDraft(event)
        is Event.MessagesShowed -> messagesShowed(event)
        is Event.MessageChanged -> messageChanged(event)
        is Event.SendFeedback -> sendFeedback(event)
        is Event.AttachFiles -> attachFiles(event)
        is Event.DetachFile -> detachFile(event)
        is Event.MessageButtonClick -> messageButtonClick(event)
        is Event.SendAgain -> sendAgain(event)
        is Event.RemoveMessage -> removeMessage(event)
        is Event.ShowToBottomButton -> showToBottomButton(event)
        is Event.ShowAttachmentPanel -> showAttachmentPanel(event)
        is Event.FormChanged -> formChanged(event)
        is Event.FormApplyClick -> formApplyClick(event)
        is Event.FormListClicked -> formListClicked(event)
        Event.SendDraft -> sendDraft()
    }

    private fun State.formApplyClick(event: Event.FormApplyClick): State {
        val form = formMap[event.messageId]
        when (form?.state) {
            UsedeskForm.State.LOADED,
            UsedeskForm.State.SENDING_FAILED -> usedeskChat.send(form)
            UsedeskForm.State.LOADING_FAILED -> usedeskChat.loadForm(form.id)
            else -> {}
        }
        return this
    }

    private fun State.formListClicked(event: Event.FormListClicked) = copy(
        formSelector = FormSelector(
            event.messageId,
            event.list,
            when (event.list.parentId) {
                null -> null
                else -> {
                    val form = formMap[event.messageId]
                    form?.fields
                        ?.filterIsInstance<Field.List>()
                        ?.firstOrNull { it.id == event.list.parentId }
                        ?.selected?.id
                }
            }
        )
    )

    private fun List<Field.List>.makeNewLists(list: Field.List) =
        mutableMapOf(list.id to list).apply {
            var parent: Field.List? = list
            while (parent != null) {
                val child = firstOrNull { (it as? Field.List)?.parentId == parent?.id }
                val childSelected = child?.selected
                val parentSelected = parent.selected
                parent = child?.copy(
                    selected = when {
                        parentSelected != null &&
                                childSelected != null &&
                                (childSelected.parentItemsId.isEmpty() ||
                                        parentSelected.id in childSelected.parentItemsId) -> childSelected
                        else -> null
                    }
                )
                if (parent != null) {
                    put(parent.id, parent)
                }
            }
        }

    private fun State.formChanged(event: Event.FormChanged): State =
        when (val form = formMap[event.messageId]) {
            null -> {
                this
            }
            else -> {
                val lists = form.fields.filterIsInstance<Field.List>()
                val newField = when (event.field) {
                    is Field.CheckBox -> event.field.copy(hasError = false)
                    is Field.List -> event.field.copy(hasError = false)
                    is Field.Text -> event.field.copy(hasError = false)
                }
                val newFields = when (newField) {
                    is Field.List -> lists.makeNewLists(newField)
                    else -> mapOf(newField.id to newField)
                }
                val newForm = form.copy(
                    fields = form.fields.map { field -> newFields[field.id] ?: field },
                    state = when (form.state) {
                        UsedeskForm.State.SENDING_FAILED -> UsedeskForm.State.LOADED
                        else -> form.state
                    }
                )
                usedeskChat.saveForm(newForm)
                copy(
                    formMap = formMap.toMutableMap().apply {
                        put(event.messageId, newForm)
                    },
                    formSelector = null
                )
            }
        }

    private fun State.showToBottomButton(event: Event.ShowToBottomButton) =
        copy(fabToBottom = event.show)

    private fun State.showAttachmentPanel(event: Event.ShowAttachmentPanel) =
        copy(attachmentPanelVisible = event.show)

    private fun State.removeMessage(event: Event.RemoveMessage) = apply {
        usedeskChat.removeMessage(event.id)
    }

    private fun State.sendAgain(event: Event.SendAgain) = apply {
        usedeskChat.sendAgain(event.id)
    }

    private fun State.sendDraft() = copy(
        messageDraft = UsedeskMessageDraft(),
        goToBottom = UsedeskEvent(Unit)
    ).apply {
        usedeskChat.sendMessageDraft()
    }

    private fun State.messageButtonClick(event: Event.MessageButtonClick) = when {
        event.button.url.isNotEmpty() -> copy(openUrl = UsedeskEvent(event.button.url))
        else -> copy(goToBottom = UsedeskEvent(Unit)).apply {
            usedeskChat.send(event.button.name)
        }
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

    private fun State.messagesShowed(event: Event.MessagesShowed): State {
        val lastMessageIndex = chatItems.indices.indexOfLast { i ->
            i <= event.messagesRange.last && chatItems[i] is ChatItem.Message
        }
        if (lastMessageIndex + ITEMS_UNTIL_LAST >= chatItems.size &&
            !previousLoading
            && hasPreviousMessages
        ) {
            usedeskChat.loadPreviousMessagesPage()
        }
        val agentMessages = event.messagesRange
            .map { chatItems.getOrNull(it) }
        agentMessages.forEach {
            val message = (it as? ChatItem.Message.Agent)?.message
            if (message is UsedeskMessageAgentText && message.fieldsInfo.isNotEmpty()) {
                val form = formMap[message.id]
                if (form == null || form.state == UsedeskForm.State.NOT_LOADED) {
                    usedeskChat.loadForm(message.id)
                }
            }
        }
        val agentMessageShowed = agentMessages
            .firstOrNull { it is ChatItem.Message.Agent }
        val newAgentIndexShowed = when (agentMessageShowed) {
            null -> this.agentMessageShowed
            else -> min(this.agentMessageShowed, chatItems.indexOf(agentMessageShowed))
        }
        return copy(
            fabToBottom = event.messagesRange.first > 0,
            agentMessageShowed = newAgentIndexShowed
        )
    }

    private fun State.messageDraft(event: Event.MessageDraft) =
        copy(messageDraft = event.messageDraft)

    private fun State.getNewAgentIndexShowed(newAgentItems: List<ChatItem.Message.Agent>): Int =
        when (val lastMessage = agentMessages.getOrNull(agentMessageShowed)) {
            null -> 0
            else -> newAgentItems.indexOfFirst { it.message.id == lastMessage.message.id }
        }

    private fun State.chatModel(event: Event.ChatModel): State = when {
        event.model.messages == messages &&
                event.model.formMap == formMap &&
                event.model.previousPageIsAvailable == hasPreviousMessages &&
                event.model.thumbnailMap == thumbnailMap -> this
        else -> {
            val newChatItems = event.model.messages.convert(
                event.model.previousPageIsAvailable,
                groupAgentMessages
            )
            val newAgentMessages = newChatItems.filterIsInstance<ChatItem.Message.Agent>()
            copy(
                agentMessages = newAgentMessages,
                chatItems = newChatItems,
                formMap = event.model.formMap.map {
                    val stateForm = formMap[it.key]
                    it.key to when {
                        stateForm?.state == it.value.state
                                && it.value.fields.all { field ->
                            field.hasError == stateForm.fields.firstOrNull { it.id == field.id }?.hasError
                        } -> stateForm
                        else -> it.value
                    }
                }.toMap(),
                agentMessageShowed = getNewAgentIndexShowed(newAgentMessages),
                thumbnailMap = event.model.thumbnailMap
            )
        }
    }.copy(
        lastChatModel = event.model,
        messages = event.model.messages,
        previousLoading = event.model.previousPageIsLoading,
        hasPreviousMessages = event.model.previousPageIsAvailable
    )

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
                        is UsedeskMessageAgentText -> ChatItem.Message.Agent(
                            message,
                            lastOfGroup,
                            showName = true,
                            showAvatar = true
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

    companion object {
        private const val ITEMS_UNTIL_LAST = 5
    }
}