package ru.usedesk.sdk.appsdk.di;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ru.usedesk.sdk.data.framework.entity.response.ErrorResponse;
import ru.usedesk.sdk.data.framework.entity.response.InitChatResponse;
import ru.usedesk.sdk.data.framework.entity.response.NewMessageResponse;
import ru.usedesk.sdk.data.framework.entity.response.SendFeedbackResponse;
import ru.usedesk.sdk.data.framework.entity.response.SetEmailResponse;
import ru.usedesk.sdk.data.framework.loader.ConfigurationLoader;
import ru.usedesk.sdk.data.framework.loader.DataLoader;
import ru.usedesk.sdk.data.framework.loader.TokenLoader;
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
            bind(Gson.class).toInstance(makeGson());
        }};
    }

    private Gson makeGson() {
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ErrorResponse.class, ErrorResponse.TYPE)
                .registerTypeAdapter(InitChatResponse.class, InitChatResponse.TYPE)
                .registerTypeAdapter(SetEmailResponse.class, SetEmailResponse.TYPE)
                .registerTypeAdapter(NewMessageResponse.class, NewMessageResponse.TYPE)
                .registerTypeAdapter(SendFeedbackResponse.class, SendFeedbackResponse.TYPE)
                .create();
    }
}
