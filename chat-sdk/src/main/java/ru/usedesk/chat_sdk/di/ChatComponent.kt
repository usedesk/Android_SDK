package ru.usedesk.chat_sdk.di

import dagger.Component
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.common_sdk.di.UsedeskCommonModule

@Component(modules = [UsedeskCommonModule::class, ChatModule::class])
interface ChatComponent {

    val chatInteractor: IUsedeskChat
}