package ru.usedesk.sdk.appsdk.di;

import android.content.Context;
import android.support.annotation.NonNull;

import ru.usedesk.sdk.data.framework.ConfigurationLoader;
import ru.usedesk.sdk.data.framework.DataLoader;
import ru.usedesk.sdk.data.framework.TokenLoader;
import ru.usedesk.sdk.data.repository.UserInfoRepository;
import ru.usedesk.sdk.domain.boundaries.IUserInfoRepository;
import ru.usedesk.sdk.domain.interactor.UsedeskManager;
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
            bind(IUserInfoRepository.class).to(UserInfoRepository.class);
            bind(DataLoader.class).withName("configuration").to(ConfigurationLoader.class);
            bind(DataLoader.class).withName("token").to(TokenLoader.class);
            bind(UsedeskManager.class).to(UsedeskManager.class);
        }};
    }
}
