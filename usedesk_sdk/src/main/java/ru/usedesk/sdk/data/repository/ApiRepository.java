package ru.usedesk.sdk.data.repository;

import android.support.annotation.NonNull;

import java.util.Arrays;

import io.socket.client.Socket;
import ru.usedesk.sdk.R;
import ru.usedesk.sdk.data.framework.ResponseProcessorImpl;
import ru.usedesk.sdk.data.framework.api.EmitterListener;
import ru.usedesk.sdk.data.framework.api.HttpApi;
import ru.usedesk.sdk.data.framework.api.SocketApi;
import ru.usedesk.sdk.data.framework.entity.request.BaseRequest;
import ru.usedesk.sdk.data.framework.entity.response.BaseResponse;
import ru.usedesk.sdk.data.framework.entity.response.ErrorResponse;
import ru.usedesk.sdk.data.framework.entity.response.InitChatResponse;
import ru.usedesk.sdk.data.framework.entity.response.NewMessageResponse;
import ru.usedesk.sdk.data.framework.entity.response.SendFeedbackResponse;
import ru.usedesk.sdk.data.framework.entity.response.SetEmailResponse;
import ru.usedesk.sdk.domain.entity.Constants;
import ru.usedesk.sdk.domain.entity.UsedeskActionListener;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;

import static ru.usedesk.sdk.utils.LogUtils.LOGD;
import static ru.usedesk.sdk.utils.LogUtils.LOGE;

public class ApiRepository {
    private static final String TAG = ApiRepository.class.getSimpleName();

    private final SocketApi socketApi;
    private final HttpApi httpApi;

    private UsedeskActionListener actionListener;

    private BaseEventEmitterListener baseEventEmitterListener;
    private ConnectEmitterListener connectEmitterListener;
    private ConnectErrorEmitterListener connectErrorEmitterListener;
    private DisconnectEmitterListener disconnectEmitterListener;

    public ApiRepository(@NonNull SocketApi socketApi, @NonNull HttpApi httpApi) {
        this.socketApi = socketApi;
        this.httpApi = httpApi;
    }

    public void setActionListener(@NonNull UsedeskActionListener actionListener) {
        this.actionListener = actionListener;

        baseEventEmitterListener = new BaseEventEmitterListener(actionListener);
        connectEmitterListener = new ConnectEmitterListener(actionListener);
        connectErrorEmitterListener = new ConnectErrorEmitterListener(actionListener);
        disconnectEmitterListener = new DisconnectEmitterListener(actionListener);
    }

    public void emitterAction(BaseRequest baseRequest) {
        try {
            socketApi.emitterAction(baseRequest);
        } catch (ApiException e) {
            LOGE(TAG, e);

            actionListener.onError(e);
        }
    }

    public boolean post(String postUrl, String data) {
        return httpApi.post(postUrl, data);
    }

    public void disconnect() {
        socketApi.disconnect();
    }

    public boolean isConnected() {
        return socketApi.isConnected();
    }

    public void setSocket(String url) throws ApiException {
        socketApi.setSocket(url);
    }

    public void connect() {
        socketApi.connect();
    }


    private class ConnectEmitterListener extends EmitterListener {

        public ConnectEmitterListener(UsedeskActionListener actionListener) {
            super(actionListener, Socket.EVENT_CONNECT);
        }

        @Override
        public void call(Object... args) {
            LOGD(TAG, "ConnectEmitterListener.args = " + Arrays.toString(args));
            initChat();
        }
    }

    private class DisconnectEmitterListener extends EmitterListener {

        public DisconnectEmitterListener(UsedeskActionListener actionListener) {
            super(actionListener, Socket.EVENT_DISCONNECT);
        }

        @Override
        public void call(Object... args) {
            LOGE(TAG, "Disconnected.");

            getActionListener().onDisconnected();
        }
    }

    private class ConnectErrorEmitterListener extends EmitterListener {

        public ConnectErrorEmitterListener(UsedeskActionListener actionListener) {
            super(actionListener, Socket.EVENT_CONNECT_ERROR, Socket.EVENT_CONNECT_TIMEOUT);
        }

        @Override
        public void call(Object... args) {
            LOGE(TAG, "Error connecting: + " + Arrays.toString(args));

            getActionListener().onError(R.string.message_connecting_error);
        }
    }

    private class BaseEventEmitterListener extends EmitterListener {

        private ResponseProcessorImpl responseProcessor = new ResponseProcessorImpl();

        public BaseEventEmitterListener(UsedeskActionListener actionListener) {
            super(actionListener, Constants.EVENT_SERVER_ACTION);
        }

        @Override
        public void call(Object... args) {
            String rawResponse = args[0].toString();

            LOGD(TAG, "BaseEventEmitterListener.rawResponse = " + rawResponse);

            BaseResponse response = responseProcessor.process(rawResponse);

            if (response != null) {
                switch (response.getType()) {
                    case ErrorResponse.TYPE:
                        parseErrorResponse((ErrorResponse) response);
                        break;
                    case InitChatResponse.TYPE:
                        parseInitResponse((InitChatResponse) response);
                        break;
                    case SetEmailResponse.TYPE:
                        break;
                    case NewMessageResponse.TYPE:
                        parseNewMessageResponse((NewMessageResponse) response);
                        break;
                    case SendFeedbackResponse.TYPE:
                        parseFeedbackResponse((SendFeedbackResponse) response);
                        break;
                }
            }
        }
    }
}
