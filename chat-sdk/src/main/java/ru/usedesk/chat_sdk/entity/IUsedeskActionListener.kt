package ru.usedesk.chat_sdk.entity

import ru.usedesk.chat_sdk.domain.IUsedeskChat

interface IUsedeskActionListener {
    fun onModel(
        model: IUsedeskChat.Model,
        newMessages: List<UsedeskMessage>,
        updatedMessages: List<UsedeskMessage>,
        removedMessages: List<UsedeskMessage>
    ) = Unit

    fun onException(usedeskException: Exception) = Unit

    fun onModelUpdated(
        oldModel: IUsedeskChat.Model?,
        newModel: IUsedeskChat.Model
    ) {
        val oldMessages = oldModel?.messages
        when {
            oldMessages == newModel.messages || oldModel?.inited != true -> {
                onModel(
                    model = newModel,
                    newMessages = listOf(),
                    updatedMessages = listOf(),
                    removedMessages = listOf()
                )
            }
            else -> {
                val newMessages = mutableListOf<UsedeskMessage>()
                val updatedMessages = mutableListOf<UsedeskMessage>()
                val oldMessagesMap = oldMessages?.associateBy { it.id } ?: mapOf()
                newModel.messages.forEach { message ->
                    when (oldMessagesMap[message.id]) {
                        null -> newMessages.add(message)
                        message -> Unit
                        else -> updatedMessages.add(message)
                    }
                }
                val removedMessages = oldMessages
                    ?.filter { oldMessage -> newModel.messages.all { !oldMessage.isIdEquals(it) } }
                    ?: listOf()
                onModel(
                    newModel,
                    newMessages,
                    updatedMessages,
                    removedMessages
                )
            }
        }
    }

    private fun UsedeskMessage.isIdEquals(other: UsedeskMessage) = id == other.id ||
            this is UsedeskMessageOwner.Client && other is UsedeskMessageOwner.Client
            && localId == other.localId
}