package ru.usedesk.chat_sdk.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.di.UsedeskCommonModule
import ru.usedesk.common_sdk.di.UsedeskCustom
import javax.inject.Scope

@ChatScope
@Component(modules = [UsedeskCommonModule::class, ChatModule::class])
internal interface ChatComponent {

    val chatInteractor: IUsedeskChat

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun bindAppContext(context: Context): Builder

        @BindsInstance
        fun bindChatConfiguration(
            chatConfiguration: UsedeskChatConfiguration
        ): Builder

        @BindsInstance
        fun bindCustomMessagesRepository(
            customMessagesRepository: UsedeskCustom<IUsedeskMessagesRepository>
        ): Builder

        fun build(): ChatComponent
    }
}

@Scope
annotation class ChatScope