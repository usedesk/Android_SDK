package ru.usedesk.sdk.data.repository;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.usedesk.sdk.data.framework.api.HttpApi;
import ru.usedesk.sdk.data.framework.api.SocketApi;
import ru.usedesk.sdk.data.framework.entity.request.BaseRequest;
import ru.usedesk.sdk.domain.boundaries.IApiRepository;
import ru.usedesk.sdk.domain.entity.OnMessageListener;
import ru.usedesk.sdk.domain.entity.UsedeskActionListener;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;

import static ru.usedesk.sdk.utils.LogUtils.LOGE;

public class ApiRepository implements IApiRepository {
    private static final String TAG = ApiRepository.class.getSimpleName();

    private final SocketApi socketApi;
    private final HttpApi httpApi;

    private UsedeskActionListener actionListener;

    @Inject
    public ApiRepository(@NonNull SocketApi socketApi, @NonNull HttpApi httpApi) {
        this.socketApi = socketApi;
        this.httpApi = httpApi;
    }

    @Override
    public void setActionListener(@NonNull UsedeskActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @Override
    public void emitterAction(BaseRequest baseRequest) {
        try {
            socketApi.emitterAction(baseRequest);
        } catch (ApiException e) {
            LOGE(TAG, e);

            actionListener.onError(e);
        }
    }

    @Override
    public boolean post(String postUrl, String data) {
        return httpApi.post(postUrl, data);
    }

    @Override
    public void disconnect() {
        socketApi.disconnect();
    }

    @Override
    public boolean isConnected() {
        return socketApi.isConnected();
    }

    @Override
    public void setSocket(String url) throws ApiException {
        socketApi.setSocket(url);
    }

    @Override
    public void connect(OnMessageListener onMessageListener) {
        socketApi.connect(actionListener, onMessageListener);
    }
}
