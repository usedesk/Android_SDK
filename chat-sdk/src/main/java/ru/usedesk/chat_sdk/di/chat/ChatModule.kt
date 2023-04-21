
package ru.usedesk.chat_sdk.di.chat

import android.content.Context
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.usedesk.chat_sdk.data.repository.api.loader.MessageResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.file.FileLoader
import ru.usedesk.chat_sdk.data.repository.api.loader.file.IFileLoader
import ru.usedesk.chat_sdk.data.repository.form.FormRepository
import ru.usedesk.chat_sdk.data.repository.form.IFormRepository
import ru.usedesk.chat_sdk.data.repository.messages.CachedMessagesRepository
import ru.usedesk.chat_sdk.data.repository.messages.ICachedMessagesRepository
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.data.repository.messages.MessagesRepository
import ru.usedesk.chat_sdk.data.repository.thumbnail.IThumbnailRepository
import ru.usedesk.chat_sdk.data.repository.thumbnail.ThumbnailRepository
import ru.usedesk.chat_sdk.di.UsedeskCustom
import ru.usedesk.chat_sdk.domain.ChatInteractor
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import javax.inject.Scope

@Module(includes = [ChatModuleProvides::class, ChatModuleBinds::class])
internal interface ChatModule

@Module
internal class ChatModuleProvides {
    @[Provides ChatScope]
    fun provideMessagesRepository(
        customMessagesRepository: UsedeskCustom<IUsedeskMessagesRepository>,
        appContext: Context,
        gson: Gson,
        fileLoader: IFileLoader,
        chatConfiguration: UsedeskChatConfiguration,
        messageResponseConverter: MessageResponseConverter
    ): IUsedeskMessagesRepository = customMessagesRepository.customInstance ?: MessagesRepository(
        appContext,
        gson,
        chatConfiguration,
        messageResponseConverter
    )
}

@Module
internal interface ChatModuleBinds {
    @[Binds ChatScope]
    fun thumbnailRepository(repository: ThumbnailRepository): IThumbnailRepository

    @[Binds ChatScope]
    fun chatInteractor(interactor: ChatInteractor): IUsedeskChat

    @[Binds ChatScope]
    fun cachedMessagesRepository(interactor: CachedMessagesRepository): ICachedMessagesRepository

    @[Binds ChatScope]
    fun formRepository(repository: FormRepository): IFormRepository

    @[Binds ChatScope]
    fun fileLoader(loader: FileLoader): IFileLoader
}

@Scope
annotation class ChatScope
