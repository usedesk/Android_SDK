
package ru.usedesk.chat_gui.chat.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.usedesk.chat_gui.chat.ChatViewModel
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel

@Module(includes = [ChatUiModuleBinds::class, ChatUiModuleProvides::class])
internal interface ChatUiModule

@Module
internal class ChatUiModuleProvides

@Module
internal interface ChatUiModuleBinds {

    @[Binds IntoMap ViewModelKey(MessagesViewModel::class)]
    fun messagesViewModel(viewModel: MessagesViewModel): ViewModel

    @[Binds IntoMap ViewModelKey(ChatViewModel::class)]
    fun chatViewModel(viewModel: ChatViewModel): ViewModel
}