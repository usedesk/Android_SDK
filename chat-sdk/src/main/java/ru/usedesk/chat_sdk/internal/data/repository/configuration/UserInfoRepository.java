package ru.usedesk.chat_sdk.internal.data.repository.configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.internal.data.framework.info.DataLoader;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException;

@Singleton
public class UserInfoRepository implements IUserInfoRepository {

    private final DataLoader<UsedeskChatConfiguration> configurationDataLoader;
    private final DataLoader<String> tokenDataLoader;

    @Inject
    UserInfoRepository(@Named("configuration") DataLoader<UsedeskChatConfiguration> configurationDataLoader,
                       @Named("token") DataLoader<String> tokenDataLoader) {
        this.configurationDataLoader = configurationDataLoader;
        this.tokenDataLoader = tokenDataLoader;
    }

    @Override
    @NonNull
    public String getToken() throws UsedeskDataNotFoundException {
        return tokenDataLoader.getData();
    }

    @Override
    public void setToken(@Nullable String token) {
        tokenDataLoader.setData(token);
    }

    @Override
    @NonNull
    public UsedeskChatConfiguration getConfiguration() throws UsedeskDataNotFoundException {
        return configurationDataLoader.getData();
    }

    @Override
    public void setConfiguration(@Nullable UsedeskChatConfiguration configuration) {
        configurationDataLoader.setData(configuration);
    }
}
