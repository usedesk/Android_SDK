package ru.usedesk.sdk.data.repository;

import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.usedesk.sdk.data.framework.DataLoader;
import ru.usedesk.sdk.domain.boundaries.IUserInfoRepository;
import ru.usedesk.sdk.domain.entity.UsedeskConfiguration;
import ru.usedesk.sdk.domain.entity.exceptions.DataNotFoundException;

@Singleton
public class UserInfoRepository implements IUserInfoRepository {

    private final DataLoader<UsedeskConfiguration> configurationDataLoader;
    private final DataLoader<String> tokenDataLoader;

    @Inject
    public UserInfoRepository(@NonNull DataLoader<UsedeskConfiguration> configurationDataLoader,
                              @NonNull DataLoader<String> tokenDataLoader) {
        this.configurationDataLoader = configurationDataLoader;
        this.tokenDataLoader = tokenDataLoader;
    }

    @Override
    @NonNull
    public String getToken() throws DataNotFoundException {
        return tokenDataLoader.getData();
    }

    @Override
    public void setToken(@NonNull String token) {
        tokenDataLoader.setData(token);
    }

    @Override
    @NonNull
    public UsedeskConfiguration getConfiguration() throws DataNotFoundException {
        return configurationDataLoader.getData();
    }

    @Override
    public void setConfiguration(@NonNull UsedeskConfiguration configuration) {
        configurationDataLoader.setData(configuration);
    }
}
