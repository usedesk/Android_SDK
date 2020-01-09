package ru.usedesk.chat_sdk.internal.data.repository.api;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;

import ru.usedesk.chat_sdk.external.entity.Feedback;
import ru.usedesk.chat_sdk.external.entity.OfflineForm;
import ru.usedesk.chat_sdk.external.entity.OnMessageListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.external.entity.UsedeskFile;
import ru.usedesk.chat_sdk.internal.data.framework.retrofit.HttpApi;
import ru.usedesk.chat_sdk.internal.data.framework.socket.SocketApi;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.BaseRequest;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.InitChatRequest;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.RequestMessage;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.SendFeedbackRequest;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.SendMessageRequest;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.SetEmailRequest;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskSocketException;

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
        } catch (UsedeskSocketException e) {
            actionListener.onException(e);
        }
    }

    @Override
    public void initChat(String token, UsedeskChatConfiguration usedeskConfiguration) {
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
    public void sendUserEmail(String token, String email, String name, Long phone, Long additionalId) {
        emitterAction(new SetEmailRequest(token, email, name, phone, additionalId));
    }

    @Override
    public void post(UsedeskChatConfiguration configuration, OfflineForm offlineForm) throws UsedeskHttpException {
        try {
            URL url = new URL(configuration.getOfflineFormUrl());
            String postUrl = String.format(OFFLINE_FORM_PATH, url.getHost());
            if (!httpApi.post(postUrl, offlineForm)) {
                throw new UsedeskHttpException(UsedeskHttpException.Error.IO_ERROR);
            }
        } catch (IOException e) {
            throw new UsedeskHttpException(UsedeskHttpException.Error.IO_ERROR, e.getMessage());
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
    public void setSocket(String url) throws UsedeskSocketException {
        socketApi.setSocket(url);
    }

    @Override
    public void connect(OnMessageListener onMessageListener) {
        socketApi.connect(actionListener, onMessageListener);
    }
}
