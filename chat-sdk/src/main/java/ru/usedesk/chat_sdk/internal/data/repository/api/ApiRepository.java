package ru.usedesk.chat_sdk.internal.data.repository.api;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;

import ru.usedesk.chat_sdk.external.entity.Feedback;
import ru.usedesk.chat_sdk.external.entity.OfflineForm;
import ru.usedesk.chat_sdk.external.entity.OnMessageListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.external.entity.UsedeskFile;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.chat_sdk.internal.data.framework.fileinfo.IFileInfoLoader;
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.IHttpApiLoader;
import ru.usedesk.chat_sdk.internal.data.framework.socket.SocketApi;
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
    private final IHttpApiLoader httpApiLoader;
    private final IFileInfoLoader fileInfoLoader;

    @Inject
    ApiRepository(@NonNull SocketApi socketApi, @NonNull IHttpApiLoader httpApiLoader, @NonNull IFileInfoLoader fileInfoLoader) {
        this.socketApi = socketApi;
        this.httpApiLoader = httpApiLoader;
        this.fileInfoLoader = fileInfoLoader;
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
    public void init(@NonNull UsedeskChatConfiguration configuration, String token) {
        socketApi.emitterActionSafe(new InitChatRequest(token, configuration.getCompanyId(), configuration.getUrl()));
    }

    @Override
    public void send(@NonNull String token, @NonNull Feedback feedback) throws UsedeskException {
        checkConnection();

        socketApi.emitterAction(new SendFeedbackRequest(token, feedback));
    }

    @Override
    public void send(@NonNull String token, @NonNull String text) throws UsedeskException {
        checkConnection();

        socketApi.emitterAction(new SendMessageRequest(token, new RequestMessage(text)));
    }

    private void checkConnection() throws UsedeskSocketException {
        if (!isConnected()) {
            throw new UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED);
        }
    }

    @Override
    public void send(@NonNull String token, @NonNull UsedeskFileInfo usedeskFileInfo) throws UsedeskException {
        checkConnection();

        UsedeskFile usedeskFile = fileInfoLoader.getFrom(usedeskFileInfo);
        socketApi.emitterAction(new SendMessageRequest(token, new RequestMessage(usedeskFile)));
    }

    @Override
    public void send(@NonNull String token, @NonNull String email, String name, Long phone, Long additionalId) {
        socketApi.emitterActionSafe(new SetEmailRequest(token, email, name, phone, additionalId));
    }

    @Override
    public void send(@NonNull UsedeskChatConfiguration configuration, @NonNull OfflineForm offlineForm) throws UsedeskException {
        try {
            URL url = new URL(configuration.getOfflineFormUrl());
            String postUrl = String.format(OFFLINE_FORM_PATH, url.getHost());
            httpApiLoader.post(postUrl, offlineForm);
        } catch (IOException e) {
            throw new UsedeskHttpException(UsedeskHttpException.Error.IO_ERROR, e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        socketApi.disconnect();
    }
}
