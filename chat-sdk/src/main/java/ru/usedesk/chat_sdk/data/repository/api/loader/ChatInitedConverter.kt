package ru.usedesk.chat_sdk.data.repository.api.loader

import ru.usedesk.chat_sdk._entity.ChatInited
import ru.usedesk.chat_sdk.data.Converter
import ru.usedesk.chat_sdk.data._entity.Message
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat.InitChatResponse
import ru.usedesk.chat_sdk.entity.UsedeskChatItem
import toothpick.InjectConstructor

@InjectConstructor
internal class ChatInitedConverter(
        private val chatItemConverter: ChatItemConverter
) : Converter<InitChatResponse, ChatInited>() {

    override fun convert(from: InitChatResponse): ChatInited {
        return ChatInited(
                from.token!!,
                from.setup!!.noOperators == true,
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