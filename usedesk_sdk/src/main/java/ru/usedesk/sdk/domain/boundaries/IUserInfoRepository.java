package ru.usedesk.sdk.domain.boundaries;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.domain.entity.UsedeskConfiguration;
import ru.usedesk.sdk.domain.entity.exceptions.DataNotFoundException;

public interface IUserInfoRepository {

    @NonNull
    String getToken() throws DataNotFoundException;

    void setToken(@NonNull String token) throws DataNotFoundException;

    @NonNull
    UsedeskConfiguration getConfiguration() throws DataNotFoundException;

    void setConfiguration(@NonNull UsedeskConfiguration configuration);
}
