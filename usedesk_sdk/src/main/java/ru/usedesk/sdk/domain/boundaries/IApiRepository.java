package ru.usedesk.sdk.domain.boundaries;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.data.framework.entity.request.BaseRequest;
import ru.usedesk.sdk.domain.entity.OnMessageListener;
import ru.usedesk.sdk.domain.entity.UsedeskActionListener;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;

public interface IApiRepository {

    void setActionListener(@NonNull UsedeskActionListener actionListener);

    void emitterAction(BaseRequest baseRequest);

    boolean post(String postUrl, String data);

    void disconnect();

    boolean isConnected();

    void setSocket(String url) throws ApiException;

    void connect(OnMessageListener onMessageListener);
}