package ru.usedesk.chat_sdk.internal.data.repository.configuration;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.usedesk.chat_sdk.external.entity.UsedeskConfiguration;
import ru.usedesk.common_sdk.external.entity.exceptions.DataNotFoundException;


public interface IUserInfoRepository {

    @NonNull
    String getToken() throws DataNotFoundException;

    void setToken(@Nullable String token);

    @NonNull
    UsedeskConfiguration getConfiguration() throws DataNotFoundException;

    void setConfiguration(@Nullable UsedeskConfiguration configuration);
}
