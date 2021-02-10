package ru.usedesk.chat_sdk.internal.di;

import android.content.ContentResolver;
import android.content.Context;

import com.google.gson.Gson;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import ru.usedesk.chat_sdk.external.IUsedeskChat;
import ru.usedesk.chat_sdk.external.entity.IUsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListenerRx;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.external.service.notifications.presenter.UsedeskNotificationsPresenter;
import ru.usedesk.chat_sdk.internal.data.framework.api.apifile.FileApi;
import ru.usedesk.chat_sdk.internal.data.framework.api.apifile.IFileApi;
import ru.usedesk.chat_sdk.internal.data.framework.configuration.ConfigurationLoader;
import ru.usedesk.chat_sdk.internal.data.framework.file.FileLoader;
import ru.usedesk.chat_sdk.internal.data.framework.file.IFileLoader;
import ru.usedesk.chat_sdk.internal.data.framework.fileinfo.FileInfoLoader;
import ru.usedesk.chat_sdk.internal.data.framework.fileinfo.IFileInfoLoader;
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.HttpApiLoader;
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.IHttpApiLoader;
import ru.usedesk.chat_sdk.internal.data.framework.info.DataLoader;
import ru.usedesk.chat_sdk.internal.data.framework.loader.TokenLoader;
import ru.usedesk.chat_sdk.internal.data.framework.multipart.IMultipartConverter;
import ru.usedesk.chat_sdk.internal.data.framework.multipart.MultipartConverter;
import ru.usedesk.chat_sdk.internal.data.framework.socket.SocketApi;
import ru.usedesk.chat_sdk.internal.data.repository.api.ApiRepository;
import ru.usedesk.chat_sdk.internal.data.repository.api.IApiRepository;
import ru.usedesk.chat_sdk.internal.data.repository.configuration.IUserInfoRepository;
import ru.usedesk.chat_sdk.internal.data.repository.configuration.UserInfoRepository;
import ru.usedesk.chat_sdk.internal.domain.ChatInteractor;
import ru.usedesk.common_sdk.internal.api.IUsedeskApiFactory;
import ru.usedesk.common_sdk.internal.api.UsedeskApiFactory;
import ru.usedesk.common_sdk.internal.api.UsedeskOkHttpClientFactory;
import toothpick.config.Module;

class MainModule extends Module {

    MainModule(@NonNull Context appContext,
               @NonNull UsedeskChatConfiguration usedeskChatConfiguration,
               @NonNull IUsedeskActionListener actionListener) {
        bind(Context.class).toInstance(appContext);
        bind(ContentResolver.class).toInstance(appContext.getContentResolver());
        bind(UsedeskChatConfiguration.class).toInstance(usedeskChatConfiguration);
        bind(IUsedeskActionListener.class).toInstance(actionListener);

        bind(IUsedeskChat.class).to(ChatInteractor.class).singleton();

        bind(IUserInfoRepository.class).to(UserInfoRepository.class).singleton();
        bind(IApiRepository.class).to(ApiRepository.class).singleton();

        bind(DataLoader.class).withName("configuration").to(ConfigurationLoader.class).singleton();
        bind(DataLoader.class).withName("token").to(TokenLoader.class).singleton();

        bind(IMultipartConverter.class).to(MultipartConverter.class).singleton();
        bind(IFileApi.class).to(FileApi.class).singleton();
        bind(IUsedeskApiFactory.class).to(UsedeskApiFactory.class).singleton();
        bind(IFileLoader.class).to(FileLoader.class).singleton();
        bind(SocketApi.class).to(SocketApi.class).singleton();
        bind(IFileInfoLoader.class).to(FileInfoLoader.class).singleton();
        bind(IHttpApiLoader.class).to(HttpApiLoader.class).singleton();

        bind(UsedeskOkHttpClientFactory.class).to(UsedeskOkHttpClientFactory.class).singleton();
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
