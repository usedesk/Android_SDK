package ru.usedesk.chat_sdk.internal.di

import android.content.Context
import com.google.gson.Gson
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.usedesk.chat_sdk.external.IUsedeskChat
import ru.usedesk.chat_sdk.external.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.external.service.notifications.presenter.UsedeskNotificationsPresenter
import ru.usedesk.chat_sdk.internal.data.framework._extra.retrofit.HttpApiFactory
import ru.usedesk.chat_sdk.internal.data.framework._extra.retrofit.IHttpApiFactory
import ru.usedesk.chat_sdk.internal.data.framework.configuration.ConfigurationLoader
import ru.usedesk.chat_sdk.internal.data.framework.configuration.IConfigurationLoader
import ru.usedesk.chat_sdk.internal.data.framework.fileinfo.FileInfoLoader
import ru.usedesk.chat_sdk.internal.data.framework.fileinfo.IFileInfoLoader
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.HttpApiLoader
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.IHttpApiLoader
import ru.usedesk.chat_sdk.internal.data.framework.token.ITokenLoader
import ru.usedesk.chat_sdk.internal.data.framework.token.TokenLoader
import ru.usedesk.chat_sdk.internal.data.repository.api.ApiRepository
import ru.usedesk.chat_sdk.internal.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.internal.data.repository.api.loader.SocketApi
import ru.usedesk.chat_sdk.internal.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.internal.data.repository.configuration.UserInfoRepository
import ru.usedesk.chat_sdk.internal.domain.ChatInteractor
import toothpick.config.Module

internal class MainModule(
        appContext: Context,
        usedeskChatConfiguration: UsedeskChatConfiguration,
        actionListener: IUsedeskActionListener
) : Module() {

    init {
        bind(Context::class.java).toInstance(appContext)
        bind(UsedeskChatConfiguration::class.java).toInstance(usedeskChatConfiguration)
        bind(IUsedeskActionListener::class.java).toInstance(actionListener)
        bind(Scheduler::class.java).withName("work").toInstance(Schedulers.io())
        bind(Scheduler::class.java).withName("main").toInstance(AndroidSchedulers.mainThread())
        bind(Gson::class.java).toInstance(gson())

        bind(SocketApi::class.java).to(SocketApi::class.java).singleton()
        bind(IConfigurationLoader::class.java).to(ConfigurationLoader::class.java).singleton()
        bind(ITokenLoader::class.java).to(TokenLoader::class.java).singleton()
        bind(IFileInfoLoader::class.java).to(FileInfoLoader::class.java).singleton()
        bind(IHttpApiLoader::class.java).to(HttpApiLoader::class.java).singleton()

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