package ru.usedesk.sdk.internal.data.repository.api;

import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import ru.usedesk.sdk.external.entity.chat.Feedback;
import ru.usedesk.sdk.external.entity.chat.OfflineForm;
import ru.usedesk.sdk.external.entity.chat.OnMessageListener;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListener;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.external.entity.exceptions.ApiException;
import ru.usedesk.sdk.internal.data.framework.api.standard.HttpApi;
import ru.usedesk.sdk.internal.data.framework.api.standard.SocketApi;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.request.BaseRequest;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.request.InitChatRequest;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.request.RequestMessage;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.request.SendFeedbackRequest;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.request.SendMessageRequest;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.request.SetEmailRequest;
import ru.usedesk.sdk.internal.domain.repositories.chat.IApiRepository;

public class ApiRepository implements IApiRepository {
    private static final String OFFLINE_FORM_PATH = "https://%1s/widget.js/post";

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
            e.printStackTrace();

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
            URL url = new URL(configuration.getOfflineFormUrl());
            String postUrl = String.format(OFFLINE_FORM_PATH, url.getHost());
            return httpApi.post(postUrl, offlineForm);
        } catch (MalformedURLException e) {
            e.printStackTrace();
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
