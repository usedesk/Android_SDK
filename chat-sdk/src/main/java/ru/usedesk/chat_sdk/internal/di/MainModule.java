package ru.usedesk.chat_sdk.internal.di;

import android.content.Context;

import com.google.gson.Gson;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import ru.usedesk.chat_sdk.external.IUsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListenerRx;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.external.service.notifications.presenter.UsedeskNotificationsPresenter;
import ru.usedesk.chat_sdk.internal.data.framework._extra.retrofit.HttpApiFactory;
import ru.usedesk.chat_sdk.internal.data.framework._extra.retrofit.IHttpApiFactory;
import ru.usedesk.chat_sdk.internal.data.framework.configuration.ConfigurationLoader;
import ru.usedesk.chat_sdk.internal.data.framework.fileinfo.FileInfoLoader;
import ru.usedesk.chat_sdk.internal.data.framework.fileinfo.IFileInfoLoader;
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.HttpApiLoader;
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.IHttpApiLoader;
import ru.usedesk.chat_sdk.internal.data.framework.info.DataLoader;
import ru.usedesk.chat_sdk.internal.data.framework.loader.TokenLoader;
import ru.usedesk.chat_sdk.internal.data.framework.socket.SocketApi;
import ru.usedesk.chat_sdk.internal.data.repository.api.ApiRepository;
import ru.usedesk.chat_sdk.internal.data.repository.api.IApiRepository;
import ru.usedesk.chat_sdk.internal.data.repository.configuration.IUserInfoRepository;
import ru.usedesk.chat_sdk.internal.data.repository.configuration.UserInfoRepository;
import ru.usedesk.chat_sdk.internal.domain.ChatSdkInteractor;
import toothpick.config.Module;

class MainModule extends Module {

    MainModule(@NonNull Context appContext, @NonNull UsedeskChatConfiguration usedeskChatConfiguration,
               @NonNull UsedeskActionListener actionListener) {
        bind(Context.class).toInstance(appContext);
        bind(UsedeskChatConfiguration.class).toInstance(usedeskChatConfiguration);
        bind(UsedeskActionListener.class).toInstance(actionListener);

        bind(IUsedeskChatSdk.class).to(ChatSdkInteractor.class).singleton();

        bind(IUserInfoRepository.class).to(UserInfoRepository.class).singleton();
        bind(IApiRepository.class).to(ApiRepository.class).singleton();

        bind(DataLoader.class).withName("configuration").to(ConfigurationLoader.class).singleton();
        bind(DataLoader.class).withName("token").to(TokenLoader.class).singleton();

        bind(SocketApi.class).to(SocketApi.class).singleton();
        bind(IFileInfoLoader.class).to(FileInfoLoader.class).singleton();
        bind(IHttpApiLoader.class).to(HttpApiLoader.class).singleton();

        bind(IHttpApiFactory.class).to(HttpApiFactory.class).singleton();
        bind(Gson.class).toInstance(gson());

        bind(Scheduler.class).withName("work").toInstance(Schedulers.io());
        bind(Scheduler.class).withName("main").toInstance(AndroidSchedulers.mainThread());

        //tmp for service
        bind(UsedeskNotificationsPresenter.class).to(UsedeskNotificationsPresenter.class).singleton();
        bind(UsedeskActionListenerRx.class).to(UsedeskActionListenerRx.class).singleton();
    }

    private Gson gson() {
        return new Gson();
    }
}
