
package ru.usedesk.chat_sdk.entity

import ru.usedesk.chat_sdk.domain.IUsedeskChat

interface IUsedeskActionListener {
    fun onModel(
        model: IUsedeskChat.Model,
        newMessages: List<UsedeskMessage>,
        updatedMessages: List<UsedeskMessage>,
        removedMessages: List<UsedeskMessage>
    ) {
    }

    fun onException(usedeskException: Exception) {}

    fun onModelUpdated(
        oldModel: IUsedeskChat.Model?,
        newModel: IUsedeskChat.Model
    ) {
        val oldMessages = oldModel?.messages
        when {
            oldMessages == newModel.messages || oldModel?.inited != true -> {
                onModel(
                    newModel,
                    listOf(),
                    listOf(),
                    listOf()
                )
            }
            else -> {
                val newMessages = mutableListOf<UsedeskMessage>()
                val updatedMessages = mutableListOf<UsedeskMessage>()
                val oldestMessageId = oldMessages?.firstOrNull()?.id ?: 0
                newModel.messages.forEach { message ->
                    if (message.id < 0 || message.id >= oldestMessageId) {
                        val oldMessage = oldMessages?.firstOrNull { message.isIdEquals(it) }
                        when {
                            oldMessage == null -> newMessages.add(message)
                            oldMessage != message -> updatedMessages.add(message)
                        }
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