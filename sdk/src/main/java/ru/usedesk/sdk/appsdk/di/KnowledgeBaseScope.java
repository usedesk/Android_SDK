package ru.usedesk.sdk.appsdk.di;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import javax.inject.Named;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import ru.usedesk.sdk.data.repository.knowledgebase.IApiLoader;
import ru.usedesk.sdk.data.repository.knowledgebase.KnowledgeBaseInfoRepository;
import ru.usedesk.sdk.data.repository.knowledgebase.KnowledgeBaseRepository;
import ru.usedesk.sdk.data.framework.api.retrofit.ApiLoader;
import ru.usedesk.sdk.data.framework.api.retrofit.ApiRetrofit;
import ru.usedesk.sdk.data.framework.api.retrofit.entity.RetrofitEnumConverterFactory;
import ru.usedesk.sdk.data.framework.loader.ConfigurationLoader;
import ru.usedesk.sdk.data.framework.loader.KnowledgeBaseConfigurationLoader;
import ru.usedesk.sdk.data.framework.loader.TokenLoader;
import ru.usedesk.sdk.data.repository.user.info.DataLoader;
import ru.usedesk.sdk.data.repository.user.info.UserInfoRepository;
import ru.usedesk.sdk.domain.boundaries.chat.IUserInfoRepository;
import ru.usedesk.sdk.domain.boundaries.knowledge.IKnowledgeBaseInfoRepository;
import ru.usedesk.sdk.domain.boundaries.knowledge.IKnowledgeBaseRepository;
import ru.usedesk.sdk.domain.interactor.knowledgebase.IKnowledgeBaseInteractor;
import ru.usedesk.sdk.domain.interactor.knowledgebase.KnowledgeBaseInteractor;
import toothpick.config.Module;

public class KnowledgeBaseScope extends DependencyInjection {

    public KnowledgeBaseScope(Object scopeObject, Context context) {
        super(scopeObject, context);
    }

    @NonNull
    @Override
    protected Module getModule(@NonNull final Context appContext) {
        return new Module() {{
            bind(Context.class).toInstance(appContext);

            bind(IUserInfoRepository.class).to(UserInfoRepository.class);

            bind(DataLoader.class).withName("configuration").to(ConfigurationLoader.class);
            bind(DataLoader.class).withName("token").to(TokenLoader.class);

            bind(Gson.class).toInstance(gson());

            bind(IKnowledgeBaseInteractor.class).to(KnowledgeBaseInteractor.class);
            bind(IKnowledgeBaseRepository.class).to(KnowledgeBaseRepository.class);

            bind(IApiLoader.class).to(ApiLoader.class);

            bind(ApiRetrofit.class).toInstance(apiRetrofit(retrofit()));

            bind(Scheduler.class).withName("work").toInstance(Schedulers.io());
            bind(Scheduler.class).withName("main").toInstance(AndroidSchedulers.mainThread());

            bind(IKnowledgeBaseInfoRepository.class).to(KnowledgeBaseInfoRepository.class);
            bind(DataLoader.class).withName("knowledgeBaseConfiguration").to(KnowledgeBaseConfigurationLoader.class);
        }};
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
                .addConverterFactory(retrofitEnumConverterFactory())
                .build();
    }

    private RetrofitEnumConverterFactory retrofitEnumConverterFactory() {
        return new RetrofitEnumConverterFactory();
    }

    private ScalarsConverterFactory scalarsConverterFactory() {
        return ScalarsConverterFactory.create();
    }

    private ApiRetrofit apiRetrofit(@NonNull final Retrofit retrofit) {
        return retrofit.create(ApiRetrofit.class);
    }

    private Gson gson() {
        return new Gson();
    }
}
