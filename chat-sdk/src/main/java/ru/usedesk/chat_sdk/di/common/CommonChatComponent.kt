package ru.usedesk.chat_sdk.di.common

import android.content.ContentResolver
import android.content.Context
import com.google.gson.Gson
import dagger.BindsInstance
import dagger.Component
import ru.usedesk.chat_sdk.data.repository._extra.ChatDatabase
import ru.usedesk.chat_sdk.data.repository.api.ChatApi
import ru.usedesk.chat_sdk.data.repository.api.loader.IInitChatResponseConverter
import ru.usedesk.chat_sdk.data.repository.api.loader.IMessageResponseConverter
import ru.usedesk.chat_sdk.data.repository.configuration.UserInfoRepository
import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.ConfigurationsLoader
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.api.IUsedeskOkHttpClientFactory
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter
import ru.usedesk.common_sdk.di.UsedeskCommonModule

@CommonChatScope
@Component(modules = [UsedeskCommonModule::class, CommonChatModule::class])
internal interface CommonChatComponent : CommonChatDeps {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance appContext: Context,
            @BindsInstance chatConfiguration: UsedeskChatConfiguration
        ): CommonChatComponent
    }

    companion object {
        var commonChatComponent: CommonChatComponent? = null
            private set

        fun open(
            context: Context,
            chatConfiguration: UsedeskChatConfiguration
        ) = commonChatComponent ?: DaggerCommonChatComponent.factory()
            .create(
                context.applicationContext,
                chatConfiguration
            ).also { commonChatComponent = it }

        fun close() {
            commonChatComponent = null
        }
    }
}

internal interface CommonChatDeps {
    val appContext: Context
    val chatConfiguration: UsedeskChatConfiguration
    val multipartConverter: IUsedeskMultipartConverter
    val gson: Gson
    val contentResolver: ContentResolver
    val apiFactory: IUsedeskApiFactory
    val apiRepository: ChatApi
    val userInfoRepository: UserInfoRepository
    val chatDatabase: ChatDatabase
    val okHttpClientFactory: IUsedeskOkHttpClientFactory
    val configurationLoader: ConfigurationsLoader
    val initChatResponseConverter: IInitChatResponseConverter
    val messageResponseConverter: IMessageResponseConverter
}