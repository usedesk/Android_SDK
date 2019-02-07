package ru.usedesk.sdk.data.repository.api;

import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import ru.usedesk.sdk.data.framework.api.HttpApi;
import ru.usedesk.sdk.data.framework.api.SocketApi;
import ru.usedesk.sdk.data.framework.api.entity.request.BaseRequest;
import ru.usedesk.sdk.data.framework.api.entity.request.InitChatRequest;
import ru.usedesk.sdk.data.framework.api.entity.request.RequestMessage;
import ru.usedesk.sdk.data.framework.api.entity.request.SendFeedbackRequest;
import ru.usedesk.sdk.data.framework.api.entity.request.SendMessageRequest;
import ru.usedesk.sdk.data.framework.api.entity.request.SetEmailRequest;
import ru.usedesk.sdk.domain.boundaries.IApiRepository;
import ru.usedesk.sdk.domain.entity.Feedback;
import ru.usedesk.sdk.domain.entity.OfflineForm;
import ru.usedesk.sdk.domain.entity.OnMessageListener;
import ru.usedesk.sdk.domain.entity.UsedeskActionListener;
import ru.usedesk.sdk.domain.entity.UsedeskConfiguration;
import ru.usedesk.sdk.domain.entity.UsedeskFile;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;

import static ru.usedesk.sdk.domain.entity.Constants.OFFLINE_FORM_PATH;
import static ru.usedesk.sdk.utils.LogUtils.LOGE;

public class ApiRepository implements IApiRepository {
    private static final String TAG = ApiRepository.class.getSimpleName();

    private final SocketApi socketApi;
    private final HttpApi httpApi;

    private UsedeskActionListener actionListener;

    @Inject
    ApiRepository(@NonNull SocketApi socketApi, @NonNull HttpApi httpApi) {
        this.socketApi = socketApi;
        this.httpApi = httpApi;
    }

    @Override
    public void setActionListener(@NonNull UsedeskActionListener actionListener) {
        this.actionListener = actionListener;
    }

    private void emitterAction(BaseRequest baseRequest) {
        try {
            socketApi.emitterAction(baseRequest);
        } catch (ApiException e) {
            LOGE(TAG, e);

            actionListener.onError(e);
        }
    }

    @Override
    public void initChat(String token, UsedeskConfiguration usedeskConfiguration) {
        emitterAction(new InitChatRequest(token, usedeskConfiguration.getCompanyId(),
                usedeskConfiguration.getUrl()));
    }

    @Override
    public void sendFeedbackMessage(String token, Feedback feedback) {
        SendFeedbackRequest sendFeedbackRequest = new SendFeedbackRequest(token, feedback);
        emitterAction(sendFeedbackRequest);
    }

    @Override
    public void sendMessageRequest(String token, String text, UsedeskFile usedeskFile) {
        emitterAction(new SendMessageRequest(token, new RequestMessage(text, usedeskFile)));
    }

    @Override
    public void sendUserEmail(String token, String email) {
        emitterAction(new SetEmailRequest(token, email));
    }

    @Override
    public boolean post(UsedeskConfiguration configuration, OfflineForm offlineForm) {
        try {
            URL url = new URL(configuration.getUrl());
            String postUrl = String.format(OFFLINE_FORM_PATH, url.getHost());
            return httpApi.post(postUrl, offlineForm);
        } catch (MalformedURLException e) {
            LOGE(TAG, e);
            return false;
        }
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
