package ru.usedesk.sdk.internal.data.framework.api.standard;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import ru.usedesk.sdk.R;
import ru.usedesk.sdk.external.entity.chat.Constants;
import ru.usedesk.sdk.external.entity.chat.OnMessageListener;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListener;
import ru.usedesk.sdk.external.entity.exceptions.ApiException;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.request.BaseRequest;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.response.BaseResponse;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.response.ErrorResponse;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.response.InitChatResponse;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.response.NewMessageResponse;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.response.SendFeedbackResponse;
import ru.usedesk.sdk.internal.data.framework.api.standard.entity.response.SetEmailResponse;
import ru.usedesk.sdk.internal.utils.LogUtils;

public class SocketApi {
    private static final String TAG = SocketApi.class.getSimpleName();

    private final Map<String, Emitter.Listener> emitterListeners = new HashMap<>(5);

    private final Gson gson;

    private Socket socket;

    private UsedeskActionListener actionListener;
    private final Emitter.Listener disconnectEmitterListener = args -> {
        LogUtils.LOGE(TAG, "Disconnected.");
        actionListener.onDisconnected();
    };
    private final Emitter.Listener connectErrorEmitterListener = args -> {
        LogUtils.LOGE(TAG, "Error connecting: + " + Arrays.toString(args));
        actionListener.onError(R.string.message_connecting_error);
    };
    private OnMessageListener onMessageListener;
    private final Emitter.Listener connectEmitterListener = args -> {
        LogUtils.LOGD(TAG, "ConnectEmitterListener.args = " + Arrays.toString(args));
        onMessageListener.onInitChat();
    };
    private final Emitter.Listener baseEventEmitterListener = args -> {
        String rawResponse = args[0].toString();

        LogUtils.LOGD(TAG, "BaseEventEmitterListener.rawResponse = " + rawResponse);

        BaseResponse response = process(rawResponse);

        if (response != null) {
            switch (response.getType()) {
                case ErrorResponse.TYPE:
                    ErrorResponse errorResponse = (ErrorResponse) response;
                    if (HttpURLConnection.HTTP_FORBIDDEN == errorResponse.getCode()) {
                        onMessageListener.onTokenError();
                    }
                    break;
                case InitChatResponse.TYPE:
                    InitChatResponse initChatResponse = (InitChatResponse) response;
                    onMessageListener.onInit(initChatResponse.getToken(),
                            initChatResponse.getSetup());
                    break;
                case SetEmailResponse.TYPE:
                    break;
                case NewMessageResponse.TYPE:
                    NewMessageResponse newMessageResponse = (NewMessageResponse) response;
                    onMessageListener.onNew(newMessageResponse.getMessage());
                    break;
                case SendFeedbackResponse.TYPE:
                    onMessageListener.onFeedback();
                    break;
            }
        }
    };

    @Inject
    SocketApi(Gson gson) {
        this.gson = gson;
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

    public void connect(UsedeskActionListener actionListener, OnMessageListener onMessageListener) {
        if (socket == null) {
            return;
        }

        this.actionListener = actionListener;
        this.onMessageListener = onMessageListener;

        emitterListeners.put(Constants.EVENT_SERVER_ACTION, baseEventEmitterListener);
        emitterListeners.put(Socket.EVENT_CONNECT_ERROR, connectErrorEmitterListener);
        emitterListeners.put(Socket.EVENT_CONNECT_TIMEOUT, connectErrorEmitterListener);
        emitterListeners.put(Socket.EVENT_DISCONNECT, disconnectEmitterListener);
        emitterListeners.put(Socket.EVENT_CONNECT, connectEmitterListener);

        for (String event : emitterListeners.keySet()) {
            socket.on(event, emitterListeners.get(event));
        }

        socket.connect();
    }

    public void disconnect() {
        for (String event : emitterListeners.keySet()) {
            socket.off(event, emitterListeners.get(event));
        }

        emitterListeners.clear();

        socket.disconnect();
    }

    public void emitterAction(BaseRequest baseRequest) throws ApiException {
        if (socket == null) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(gson.toJson(baseRequest));
            LogUtils.LOGD(TAG, "emitAction(). request = " + jsonObject);

            socket.emit(Constants.EVENT_SERVER_ACTION, jsonObject);
        } catch (JSONException e) {
            throw new ApiException(e.getMessage());
        }
    }

    private BaseResponse process(String rawResponse) {
        try {
            BaseRequest baseRequest = gson.fromJson(rawResponse, BaseRequest.class);

            if (baseRequest != null && baseRequest.getType() != null) {
                switch (baseRequest.getType()) {
                    case InitChatResponse.TYPE:
                        return gson.fromJson(rawResponse, InitChatResponse.class);
                    case ErrorResponse.TYPE:
                        return gson.fromJson(rawResponse, ErrorResponse.class);
                    case NewMessageResponse.TYPE:
                        return gson.fromJson(rawResponse, NewMessageResponse.class);
                    case SendFeedbackResponse.TYPE:
                        return gson.fromJson(rawResponse, SendFeedbackResponse.class);
                    case SetEmailResponse.TYPE:
                        return gson.fromJson(rawResponse, SetEmailResponse.class);
                }
            }
        } catch (JsonParseException e) {
            LogUtils.LOGE(TAG, e);
        }

        return null;
    }
}
