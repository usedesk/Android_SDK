
package ru.usedesk.chat_sdk.di.chat

import dagger.BindsInstance
import dagger.Component
import ru.usedesk.chat_sdk.data.repository.messages.UsedeskMessagesRepository
import ru.usedesk.chat_sdk.di.Release
import ru.usedesk.chat_sdk.di.UsedeskCustom
import ru.usedesk.chat_sdk.di.common.CommonChatComponent
import ru.usedesk.chat_sdk.di.common.CommonChatDeps
import ru.usedesk.chat_sdk.domain.UsedeskChat

@[ChatScope Component(
    modules = [ChatModule::class],
    dependencies = [CommonChatDeps::class]
)]
internal interface ChatComponent : CommonChatDeps {

    val chatInteractor: UsedeskChat

    @Component.Factory
    interface Factory {
        fun create(
            commonChatComponent: CommonChatDeps,
            @BindsInstance messagesRepository: UsedeskCustom<UsedeskMessagesRepository>
        ): ChatComponent
    }

    companion object {
        var chatComponent: ChatComponent? = null
            private set

        fun open(
            commonChatComponent: CommonChatComponent,
            messagesRepository: UsedeskCustom<UsedeskMessagesRepository>
        ) = chatComponent ?: DaggerChatComponent.factory()
            .create(
                commonChatComponent,
                messagesRepository
            ).also { chatComponent = it }

        fun close() {
            (chatComponent?.chatInteractor as? Release)?.release()
            chatComponent = null
        }
    }
}