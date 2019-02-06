package ru.usedesk.sdk.data.repository;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.usedesk.sdk.data.framework.api.HttpApi;
import ru.usedesk.sdk.data.framework.api.SocketApi;
import ru.usedesk.sdk.data.framework.entity.request.BaseRequest;
import ru.usedesk.sdk.data.framework.entity.request.InitChatRequest;
import ru.usedesk.sdk.data.framework.entity.request.SendFeedbackRequest;
import ru.usedesk.sdk.data.framework.entity.request.SendMessageRequest;
import ru.usedesk.sdk.data.framework.entity.request.SetEmailRequest;
import ru.usedesk.sdk.domain.boundaries.IApiRepository;
import ru.usedesk.sdk.domain.entity.Feedback;
import ru.usedesk.sdk.domain.entity.OnMessageListener;
import ru.usedesk.sdk.domain.entity.UsedeskActionListener;
import ru.usedesk.sdk.domain.entity.UsedeskConfiguration;
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
        emitterAction(new InitChatRequest() {{
            setToken(token);
            setCompanyId(usedeskConfiguration.getCompanyId());
            setUrl(usedeskConfiguration.getUrl());
        }});
    }

    @Override
    public void sendFeedbackMessage(String token, Feedback feedback) {
        emitterAction(new SendFeedbackRequest(feedback) {{
            setToken(token);
        }});
    }

    @Override
    public void sendMessageRequest(String token, SendMessageRequest.Message sendMessage) {
        emitterAction(new SendMessageRequest() {{
            setToken(token);
            setMessage(sendMessage);
        }});
    }

    @Override
    public void sendUserEmail(String token, String email) {
        emitterAction(new SetEmailRequest() {{
            setToken(token);
            setEmail(email);
        }});
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
