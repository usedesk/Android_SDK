package ru.usedesk.sdk.data.framework.api;

import android.support.annotation.NonNull;

import org.json.JSONException;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import ru.usedesk.sdk.R;
import ru.usedesk.sdk.data.framework.ResponseProcessorImpl;
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

public class SocketApi {
    private static final String TAG = SocketApi.class.getSimpleName();

    private Socket socket;

    private List<EmitterListener> emitterListeners = new ArrayList<>(4);

    public SocketApi() {
    }

    public boolean isConnected() {
        return socket.connected();
    }

    public void setSocket(@NonNull String url) throws ApiException {
        try {
            socket = IO.socket(url);
        } catch (URISyntaxException e) {
            throw new ApiException(e.getMessage());
        }
    }

    public void connect(UsedeskActionListener actionListener) {
        if (socket == null) {
            return;
        }

        emitterListeners.add(new BaseEventEmitterListener(actionListener));
        emitterListeners.add(new ConnectEmitterListener(actionListener));
        emitterListeners.add(new ConnectErrorEmitterListener(actionListener));
        emitterListeners.add(new DisconnectEmitterListener(actionListener));

        for (EmitterListener emitterListener : emitterListeners) {
            emitterListener.attachSocket(socket);
        }

        socket.connect();
    }

    public void disconnect() {
        for (EmitterListener emitterListener : emitterListeners) {
            emitterListener.detachSocket(socket);
        }

        emitterListeners.clear();

        socket.disconnect();
    }

    public void emitterAction(BaseRequest baseRequest) throws ApiException {
        if (socket == null) {
            return;
        }

        try {
            LOGD(TAG, "emitAction(). request = " + baseRequest.toJSONObject());

            socket.emit(Constants.EVENT_SERVER_ACTION, baseRequest.toJSONObject());
        } catch (JSONException e) {
            throw new ApiException(e.getMessage());
        }
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
