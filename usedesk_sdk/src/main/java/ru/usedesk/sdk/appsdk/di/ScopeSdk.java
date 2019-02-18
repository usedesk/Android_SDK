package ru.usedesk.sdk.appsdk.di;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import ru.usedesk.sdk.data.framework.api.retrofit.ApiLoader;
import ru.usedesk.sdk.data.framework.api.standard.HttpApi;
import ru.usedesk.sdk.data.framework.api.standard.SocketApi;
import ru.usedesk.sdk.data.framework.loader.ConfigurationLoader;
import ru.usedesk.sdk.data.framework.loader.TokenLoader;
import ru.usedesk.sdk.data.repository.api.ApiRepository;
import ru.usedesk.sdk.data.repository.knowledgebase.IApiLoader;
import ru.usedesk.sdk.data.repository.knowledgebase.KnowledgeBaseRepository;
import ru.usedesk.sdk.data.repository.user.info.DataLoader;
import ru.usedesk.sdk.data.repository.user.info.UserInfoRepository;
import ru.usedesk.sdk.domain.boundaries.IApiRepository;
import ru.usedesk.sdk.domain.boundaries.IKnowledgeBaseRepository;
import ru.usedesk.sdk.domain.boundaries.IUserInfoRepository;
import ru.usedesk.sdk.domain.interactor.UsedeskManager;
import ru.usedesk.sdk.domain.interactor.knowledgebase.IKnowledgeBaseInteractor;
import ru.usedesk.sdk.domain.interactor.knowledgebase.KnowledgeBaseInteractor;
import toothpick.config.Module;

public class ScopeSdk extends DependencyInjection {

    public ScopeSdk(Object scopeObject, Context context) {
        super(scopeObject, context);
    }

    @NonNull
    @Override
    protected Module getModule(final Context appContext) {
        return new Module() {{
            bind(Context.class).toInstance(appContext);

            bind(UsedeskManager.class).to(UsedeskManager.class);

            bind(IUserInfoRepository.class).to(UserInfoRepository.class);
            bind(IApiRepository.class).to(ApiRepository.class);

            bind(DataLoader.class).withName("configuration").to(ConfigurationLoader.class);
            bind(DataLoader.class).withName("token").to(TokenLoader.class);

            bind(SocketApi.class).to(SocketApi.class);
            bind(HttpApi.class).to(HttpApi.class);

            bind(Gson.class).toInstance(makeGson());

            bind(IKnowledgeBaseInteractor.class).to(KnowledgeBaseInteractor.class);

            bind(IKnowledgeBaseRepository.class).to(KnowledgeBaseRepository.class);

            bind(IApiLoader.class).to(ApiLoader.class);
        }};
    }

    private Gson makeGson() {
        return new Gson();
    }
}
