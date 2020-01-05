package ru.usedesk.chat_sdk.internal.di;

import android.content.Context;

import com.google.gson.Gson;

import javax.inject.Named;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import ru.usedesk.chat_sdk.external.IUsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListenerRx;
import ru.usedesk.chat_sdk.external.service.notifications.presenter.NotificationsPresenter;
import ru.usedesk.chat_sdk.internal.data.framework.configuration.ConfigurationLoader;
import ru.usedesk.chat_sdk.internal.data.framework.info.DataLoader;
import ru.usedesk.chat_sdk.internal.data.framework.loader.TokenLoader;
import ru.usedesk.chat_sdk.internal.data.framework.retrofit.HttpApi;
import ru.usedesk.chat_sdk.internal.data.framework.socket.SocketApi;
import ru.usedesk.chat_sdk.internal.data.repository.api.ApiRepository;
import ru.usedesk.chat_sdk.internal.data.repository.api.IApiRepository;
import ru.usedesk.chat_sdk.internal.data.repository.configuration.IUserInfoRepository;
import ru.usedesk.chat_sdk.internal.data.repository.configuration.UserInfoRepository;
import ru.usedesk.chat_sdk.internal.domain.ChatSdk;
import toothpick.config.Module;

public class MainModule extends Module {

    public MainModule(@NonNull Context appContext) {
        bind(Context.class).toInstance(appContext);

        bind(IUsedeskChatSdk.class).to(ChatSdk.class).singleton();

        bind(IUserInfoRepository.class).to(UserInfoRepository.class).singleton();
        bind(IApiRepository.class).to(ApiRepository.class).singleton();

        bind(DataLoader.class).withName("configuration").to(ConfigurationLoader.class).singleton();
        bind(DataLoader.class).withName("token").to(TokenLoader.class).singleton();

        bind(SocketApi.class).to(SocketApi.class).singleton();
        bind(HttpApi.class).to(HttpApi.class).singleton();

        bind(Gson.class).toInstance(gson());

        bind(Scheduler.class).withName("work").toInstance(Schedulers.io());
        bind(Scheduler.class).withName("main").toInstance(AndroidSchedulers.mainThread());

        //tmp for service
        bind(NotificationsPresenter.class).to(NotificationsPresenter.class).singleton();
        bind(UsedeskActionListenerRx.class).to(UsedeskActionListenerRx.class).singleton();
    }

    @NonNull
    @Named("serverBaseUrl")
    private String serverBaseUrl() {
        return "https://api.usedesk.ru/support/";
    }

    @NonNull
    private OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder().build();
    }


    private Retrofit retrofit() {
        return new Retrofit.Builder()
                .baseUrl(serverBaseUrl())
                .client(okHttpClient())
                .addConverterFactory(scalarsConverterFactory())
                .build();

    }

    private ScalarsConverterFactory scalarsConverterFactory() {
        return ScalarsConverterFactory.create();
    }

    private Gson gson() {
        return new Gson();
    }
}
