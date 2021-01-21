package ru.usedesk.chat_sdk.internal.data.repository.api;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import okhttp3.MultipartBody;
import ru.usedesk.chat_sdk.external.entity.IUsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.external.entity.UsedeskFeedback;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.chat_sdk.external.entity.UsedeskOfflineForm;
import ru.usedesk.chat_sdk.internal.data.framework.api.apifile.IFileApi;
import ru.usedesk.chat_sdk.internal.data.framework.file.IFileLoader;
import ru.usedesk.chat_sdk.internal.data.framework.file.entity.LoadedFile;
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.IHttpApiLoader;
import ru.usedesk.chat_sdk.internal.data.framework.multipart.IMultipartConverter;
import ru.usedesk.chat_sdk.internal.data.framework.socket.SocketApi;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.InitChatRequest;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.RequestMessage;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.SendFeedbackRequest;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.SendMessageRequest;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.SetEmailRequest;
import ru.usedesk.chat_sdk.internal.domain.entity.OnMessageListener;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskSocketException;

public class ApiRepository implements IApiRepository {
    private static final String OFFLINE_FORM_PATH = "https://%1s/widget.js/";

    private final SocketApi socketApi;
    private final IHttpApiLoader httpApiLoader;
    private final IFileApi fileApi;
    private final IFileLoader fileLoader;
    private final IMultipartConverter multipartConverter;

    @Inject
    public ApiRepository(@NonNull SocketApi socketApi,
                         @NonNull IHttpApiLoader httpApiLoader,
                         @NonNull IFileApi fileApi,
                         @NonNull IFileLoader fileLoader,
                         @NonNull IMultipartConverter multipartConverter) {
        this.socketApi = socketApi;
        this.httpApiLoader = httpApiLoader;
        this.fileApi = fileApi;
        this.fileLoader = fileLoader;
        this.multipartConverter = multipartConverter;
    }

    private boolean isConnected() {
        return socketApi.isConnected();
    }

    @Override
    public void connect(@NonNull String url, @NonNull IUsedeskActionListener actionListener,
                        @NonNull OnMessageListener onMessageListener) throws UsedeskException {
        socketApi.connect(url, actionListener, onMessageListener);
    }

    @Override
    public void init(@NonNull UsedeskChatConfiguration configuration, String token)
            throws UsedeskException {
        socketApi.sendRequest(new InitChatRequest(token, configuration.getCompanyId(),
                configuration.getSocketUrl()));
    }

    @Override
    public void send(@NonNull String token, @NonNull UsedeskFeedback feedback) throws UsedeskException {
        checkConnection();

        socketApi.sendRequest(new SendFeedbackRequest(token, feedback));
    }

    @Override
    public void send(@NonNull String token, @NonNull String text) throws UsedeskException {
        checkConnection();

        socketApi.sendRequest(new SendMessageRequest(token, new RequestMessage(text)));
    }

    private void checkConnection() throws UsedeskSocketException {
        if (!isConnected()) {
            throw new UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED);
        }
    }

    @Override
    public void send(@NonNull UsedeskChatConfiguration configuration,
                     @NonNull String token,
                     @NonNull UsedeskFileInfo usedeskFileInfo) throws UsedeskException {
        checkConnection();

        try {
            URL url = new URL(configuration.getSecureUrl());
            String postUrl = "https://" + url.getHost() + "/uapi/v1/";
            LoadedFile loadedFile = fileLoader.load(usedeskFileInfo.getUri());
            List<MultipartBody.Part> parts = new ArrayList<>();
            parts.add(multipartConverter.convert("chat_token", token));
            parts.add(multipartConverter.convert("file", loadedFile));
            fileApi.post(postUrl, parts);
        } catch (Exception e) {
            throw new UsedeskException(e.getMessage());
        }
    }

    @Override
    public void send(@NonNull String token, @NonNull String email, String name, Long phone, Long additionalId) throws UsedeskException {
        socketApi.sendRequest(new SetEmailRequest(token, email, name, phone, additionalId));
    }

    @Override
    public void send(@NonNull UsedeskChatConfiguration configuration, @NonNull UsedeskOfflineForm offlineForm) throws UsedeskException {
        try {
            URL url = new URL(configuration.getSecureUrl());
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
