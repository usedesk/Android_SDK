package ru.usedesk.chat_sdk.di

import android.content.ContentResolver
import android.content.Context
import com.google.gson.Gson
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.usedesk.chat_sdk.data.repository._extra.multipart.IMultipartConverter
import ru.usedesk.chat_sdk.data.repository._extra.multipart.MultipartConverter
import ru.usedesk.chat_sdk.data.repository._extra.retrofit.HttpApiFactory
import ru.usedesk.chat_sdk.data.repository._extra.retrofit.IHttpApiFactory
import ru.usedesk.chat_sdk.data.repository.api.ApiRepository
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.data.repository.api.loader.apifile.FileApi
import ru.usedesk.chat_sdk.data.repository.api.loader.apifile.IFileApi
import ru.usedesk.chat_sdk.data.repository.api.loader.apiofflineform.IOfflineFormApi
import ru.usedesk.chat_sdk.data.repository.api.loader.apiofflineform.OfflineFormApi
import ru.usedesk.chat_sdk.data.repository.api.loader.socket.SocketApi
import ru.usedesk.chat_sdk.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.data.repository.configuration.UserInfoRepository
import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.ConfigurationLoader
import ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration.IConfigurationLoader
import ru.usedesk.chat_sdk.data.repository.configuration.loader.token.ITokenLoader
import ru.usedesk.chat_sdk.data.repository.configuration.loader.token.TokenLoader
import ru.usedesk.chat_sdk.domain.ChatInteractor
import ru.usedesk.chat_sdk.domain.IUsedeskChat
import ru.usedesk.chat_sdk.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.service.notifications.presenter.UsedeskNotificationsPresenter
import toothpick.config.Module

internal class MainModule(
        appContext: Context,
        usedeskChatConfiguration: UsedeskChatConfiguration,
        actionListener: IUsedeskActionListener
) : Module() {

    init {
        bind(Context::class.java).toInstance(appContext)
        bind(ContentResolver::class.java).toInstance(appContext.contentResolver)
        bind(UsedeskChatConfiguration::class.java).toInstance(usedeskChatConfiguration)
        bind(IUsedeskActionListener::class.java).toInstance(actionListener)
        bind(Scheduler::class.java).withName("work").toInstance(Schedulers.io())
        bind(Scheduler::class.java).withName("main").toInstance(AndroidSchedulers.mainThread())
        bind(Gson::class.java).toInstance(gson())

        bind(SocketApi::class.java).to(SocketApi::class.java).singleton()
        bind(IConfigurationLoader::class.java).to(ConfigurationLoader::class.java).singleton()
        bind(ITokenLoader::class.java).to(TokenLoader::class.java).singleton()
        bind(IMultipartConverter::class.java).to(MultipartConverter::class.java).singleton()
        bind(IFileApi::class.java).to(FileApi::class.java).singleton()
        bind(IOfflineFormApi::class.java).to(OfflineFormApi::class.java).singleton()

        bind(IUserInfoRepository::class.java).to(UserInfoRepository::class.java).singleton()
        bind(IApiRepository::class.java).to(ApiRepository::class.java).singleton()
        bind(IHttpApiFactory::class.java).to(HttpApiFactory::class.java).singleton()

        bind(IUsedeskChat::class.java).to(ChatInteractor::class.java).singleton()

        //tmp for service
        bind(UsedeskNotificationsPresenter::class.java).to(UsedeskNotificationsPresenter::class.java).singleton()
    }

    private fun gson(): Gson {
        return Gson()
    }
}