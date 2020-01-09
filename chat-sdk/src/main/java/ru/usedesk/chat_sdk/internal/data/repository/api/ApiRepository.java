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
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.chat_sdk.internal.data.framework.retrofit.HttpApi;
import ru.usedesk.chat_sdk.internal.data.framework.socket.SocketApi;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.BaseRequest;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.InitChatRequest;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.RequestMessage;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.SendFeedbackRequest;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.SendMessageRequest;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.SetEmailRequest;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskSocketException;

public class ApiRepository implements IApiRepository {
    private static final String OFFLINE_FORM_PATH = "https://%1s/widget.js/post";

    private final SocketApi socketApi;
    private final HttpApi httpApi;

    @Inject
    ApiRepository(@NonNull SocketApi socketApi, @NonNull HttpApi httpApi) {
        this.socketApi = socketApi;
        this.httpApi = httpApi;
    }

    private void emitterAction(BaseRequest baseRequest) throws UsedeskException {
        socketApi.emitterAction(baseRequest);
    }

    private boolean isConnected() {
        return socketApi.isConnected();
    }

    @Override
    public void connect(@NonNull String url, @NonNull UsedeskActionListener actionListener,
                        @NonNull OnMessageListener onMessageListener) throws UsedeskException {
        socketApi.connect(url, actionListener, onMessageListener);
    }

    @Override
    public void init(@NonNull UsedeskChatConfiguration configuration, @NonNull String token) throws UsedeskException {
        emitterAction(new InitChatRequest(token, configuration.getCompanyId(), configuration.getUrl()));
    }

    @Override
    public void send(@NonNull String token, @NonNull Feedback feedback) throws UsedeskException {
        checkConnection();

        emitterAction(new SendFeedbackRequest(token, feedback));
    }

    @Override
    public void send(@NonNull String token, @NonNull String text) throws UsedeskException {
        checkConnection();

        emitterAction(new SendMessageRequest(token, new RequestMessage(text)));
    }

    private void checkConnection() throws UsedeskSocketException {
        if (!isConnected()) {
            throw new UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED);
        }
    }

    @Override
    public void send(@NonNull String token, @NonNull UsedeskFileInfo usedeskFileInfo) throws UsedeskException {
        checkConnection();

        //TODO: convert fileInfo to file and send

        //emitterAction(new SendMessageRequest(token, new RequestMessage(usedeskFile)));
    }

    @Override
    public void send(@NonNull String token, @NonNull String email, String name, Long phone, Long additionalId) throws UsedeskException {
        emitterAction(new SetEmailRequest(token, email, name, phone, additionalId));
    }

    @Override
    public void send(@NonNull UsedeskChatConfiguration configuration, @NonNull OfflineForm offlineForm) throws UsedeskException {
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
    public void disconnect() throws UsedeskException {
        socketApi.disconnect();
    }
}
