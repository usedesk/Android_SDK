package ru.usedesk.chat_sdk.data.repository.api.loader

import ru.usedesk.chat_sdk.data.Converter
import ru.usedesk.chat_sdk.data.repository.api.entity.ChatInited
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat.InitChatResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageResponse
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import toothpick.InjectConstructor

@InjectConstructor
internal class InitChatResponseConverter(
        private val messageResponseConverter: MessageResponseConverter
) : Converter<InitChatResponse, ChatInited>() {

    override fun convert(from: InitChatResponse): ChatInited {
        return ChatInited(
                from.token!!,
                from.setup!!.noOperators == true,
                true,
                convert(from.setup?.messages ?: listOf())
        )
    }

    private fun convert(messages: List<MessageResponse.Message?>): List<UsedeskMessage> {
        return messages.flatMap {
            convertOrNull {
                messageResponseConverter.convert(it)
            } ?: listOf()
        }
    }
}