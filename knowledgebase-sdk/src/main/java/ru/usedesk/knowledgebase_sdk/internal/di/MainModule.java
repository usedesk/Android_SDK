package ru.usedesk.knowledgebase_sdk.internal.di;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.usedesk.common_sdk.internal.api.IUsedeskApiFactory;
import ru.usedesk.common_sdk.internal.api.UsedeskApiFactory;
import ru.usedesk.common_sdk.internal.api.UsedeskOkHttpClientFactory;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskKnowledgeBaseConfiguration;
import ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.ApiLoader;
import ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.ApiRetrofit;
import ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.IApiLoader;
import ru.usedesk.knowledgebase_sdk.internal.data.repository.ApiRepository;
import ru.usedesk.knowledgebase_sdk.internal.data.repository.IKnowledgeBaseRepository;
import ru.usedesk.knowledgebase_sdk.internal.domain.KnowledgeBaseInteractor;
import toothpick.config.Module;

class MainModule extends Module {

    MainModule(@NonNull Context appContext,
               @NonNull UsedeskKnowledgeBaseConfiguration knowledgeBaseConfiguration) {
        bind(Context.class).toInstance(appContext);
        bind(UsedeskKnowledgeBaseConfiguration.class).toInstance(knowledgeBaseConfiguration);

        bind(Gson.class).toInstance(gson());

        bind(IUsedeskKnowledgeBase.class).to(KnowledgeBaseInteractor.class);
        bind(IKnowledgeBaseRepository.class).to(ApiRepository.class);

        bind(IApiLoader.class).to(ApiLoader.class);

        bind(ApiRetrofit.class).toInstance(apiRetrofit(appContext));

        bind(Scheduler.class).withName("work").toInstance(Schedulers.io());
        bind(Scheduler.class).withName("main").toInstance(AndroidSchedulers.mainThread());
    }

    private ApiRetrofit apiRetrofit(@NonNull Context appContext) {
        UsedeskOkHttpClientFactory okHttpClientFactory = new UsedeskOkHttpClientFactory(appContext);
        IUsedeskApiFactory apiFactory = new UsedeskApiFactory(gson(), okHttpClientFactory);
        return apiFactory.getInstance("https://api.usedesk.ru/support/", ApiRetrofit.class);
    }

    private Gson gson() {
        return new Gson();
    }
}
