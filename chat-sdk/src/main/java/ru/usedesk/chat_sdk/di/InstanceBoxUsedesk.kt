package ru.usedesk.chat_sdk.di

import android.content.Context
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.domain.ChatInteractor
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration

internal class InstanceBoxUsedesk(
    context: Context,
    chatConfiguration: UsedeskChatConfiguration,
    messagesRepository: IUsedeskMessagesRepository?
) {
    private var daggerChatComponent: ChatComponent?

    val chatInteractor: ChatInteractor

    init {
        val daggerChatComponent = DaggerChatComponent.builder()
            .appContext(context.applicationContext)
            .configuration(chatConfiguration)
            .customMessagesRepository(UsedeskCustom(messagesRepository))
            .build()

        this.daggerChatComponent = daggerChatComponent
        this.chatInteractor = daggerChatComponent.chatInteractor
    }

    fun release() {
        chatInteractor.release()
        daggerChatComponent = null
    }
}