package ru.usedesk.knowledgebase_sdk.internal.di;

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
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.KnowledgeBaseConfiguration;
import ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.ApiLoader;
import ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.ApiRetrofit;
import ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.IApiLoader;
import ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.entity.RetrofitEnumConverterFactory;
import ru.usedesk.knowledgebase_sdk.internal.data.repository.ApiRepository;
import ru.usedesk.knowledgebase_sdk.internal.data.repository.IKnowledgeBaseRepository;
import ru.usedesk.knowledgebase_sdk.internal.domain.KnowledgeBase;
import toothpick.config.Module;

class MainModule extends Module {

    MainModule(@NonNull Context appContext, @NonNull KnowledgeBaseConfiguration knowledgeBaseConfiguration) {
        bind(Context.class).toInstance(appContext);
        bind(KnowledgeBaseConfiguration.class).toInstance(knowledgeBaseConfiguration);

        bind(Gson.class).toInstance(gson());

        bind(IUsedeskKnowledgeBaseSdk.class).to(KnowledgeBase.class);
        bind(IKnowledgeBaseRepository.class).to(ApiRepository.class);

        bind(IApiLoader.class).to(ApiLoader.class);

        bind(ApiRetrofit.class).toInstance(apiRetrofit(retrofit()));

        bind(Scheduler.class).withName("work").toInstance(Schedulers.io());
        bind(Scheduler.class).withName("main").toInstance(AndroidSchedulers.mainThread());
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
