package ru.usedesk.chat_sdk.external;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.entity.exceptions.DataNotFoundException;

public interface IUserInfoRepository {

    @NonNull
    String getToken() throws DataNotFoundException;

    void setToken(@Nullable String token);

    @NonNull
    UsedeskConfiguration getConfiguration() throws DataNotFoundException;

    void setConfiguration(@Nullable UsedeskConfiguration configuration);
}
