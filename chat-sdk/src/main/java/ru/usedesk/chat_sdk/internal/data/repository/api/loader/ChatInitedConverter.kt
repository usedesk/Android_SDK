package ru.usedesk.chat_sdk.internal.data.repository.api.loader

import ru.usedesk.chat_sdk.external.entity.UsedeskChatItem
import ru.usedesk.chat_sdk.internal._entity.ChatInited
import ru.usedesk.chat_sdk.internal.data.Converter
import ru.usedesk.chat_sdk.internal.data.repository.api._entity.response.ChatInitedResponse
import ru.usedesk.chat_sdk.internal.domain.entity.Message
import toothpick.InjectConstructor

@InjectConstructor
internal class ChatInitedConverter(
        private val chatItemConverter: ChatItemConverter
) : Converter<ChatInitedResponse, ChatInited>() {

    override fun convert(from: ChatInitedResponse): ChatInited {
        return ChatInited(
                from.token!!,
                from.noOperators ?: false,
                from.setup!!.waitingEmail!!,
                convert(from.setup?.messages ?: listOf())
        )
    }

    private fun convert(messages: List<Message?>): List<UsedeskChatItem> {
        return messages.flatMap {
            convertOrNull {
                chatItemConverter.convert(it!!)
            } ?: listOf()
        }
    }
}