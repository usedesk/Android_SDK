
package ru.usedesk.chat_sdk.di.chat

import android.content.Context
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.usedesk.chat_sdk.data.repository.api.loader.MessageResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.file.FileLoader
import ru.usedesk.chat_sdk.data.repository.api.loader.file.FileLoaderImpl
import ru.usedesk.chat_sdk.data.repository.form.FormRepository
import ru.usedesk.chat_sdk.data.repository.form.FormRepositoryImpl
import ru.usedesk.chat_sdk.data.repository.messages.CachedMessagesRepository
import ru.usedesk.chat_sdk.data.repository.messages.CachedMessagesRepositoryImpl
import ru.usedesk.chat_sdk.data.repository.messages.UsedeskMessagesRepository
import ru.usedesk.chat_sdk.data.repository.messages.MessagesRepository
import ru.usedesk.chat_sdk.data.repository.thumbnail.ThumbnailRepository
import ru.usedesk.chat_sdk.data.repository.thumbnail.ThumbnailRepositoryImpl
import ru.usedesk.chat_sdk.di.UsedeskCustom
import ru.usedesk.chat_sdk.domain.ChatImpl
import ru.usedesk.chat_sdk.domain.UsedeskChat
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import javax.inject.Scope

@Module(includes = [ChatModuleProvides::class, ChatModuleBinds::class])
internal interface ChatModule

@Module
internal class ChatModuleProvides {
    @[Provides ChatScope]
    fun provideMessagesRepository(
        customMessagesRepository: UsedeskCustom<UsedeskMessagesRepository>,
        appContext: Context,
        gson: Gson,
        chatConfiguration: UsedeskChatConfiguration,
        messageResponseConverter: MessageResponseConverter
    ): UsedeskMessagesRepository = customMessagesRepository.customInstance ?: MessagesRepository(
        appContext,
        gson,
        chatConfiguration,
        messageResponseConverter
    )
}

@Module
internal interface ChatModuleBinds {
    @[Binds ChatScope]
    fun thumbnailRepository(repository: ThumbnailRepositoryImpl): ThumbnailRepository

    @[Binds ChatScope]
    fun chatInteractor(interactor: ChatImpl): UsedeskChat

    @[Binds ChatScope]
    fun cachedMessagesRepository(interactor: CachedMessagesRepositoryImpl): CachedMessagesRepository

    @[Binds ChatScope]
    fun formRepository(repository: FormRepositoryImpl): FormRepository

    @[Binds ChatScope]
    fun fileLoader(loader: FileLoaderImpl): FileLoader
}

@Scope
annotation class ChatScope
