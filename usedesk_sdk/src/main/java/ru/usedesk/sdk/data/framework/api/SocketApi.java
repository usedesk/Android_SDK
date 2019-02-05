package ru.usedesk.sdk.data.framework.api;

import android.support.annotation.NonNull;

import org.json.JSONException;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import ru.usedesk.sdk.data.framework.api.emitter.listener.BaseEventEmitterListener;
import ru.usedesk.sdk.data.framework.api.emitter.listener.ConnectEmitterListener;
import ru.usedesk.sdk.data.framework.api.emitter.listener.ConnectErrorEmitterListener;
import ru.usedesk.sdk.data.framework.api.emitter.listener.DisconnectEmitterListener;
import ru.usedesk.sdk.data.framework.api.emitter.listener.EmitterListener;
import ru.usedesk.sdk.data.framework.entity.request.BaseRequest;
import ru.usedesk.sdk.domain.entity.Constants;
import ru.usedesk.sdk.domain.entity.OnMessageListener;
import ru.usedesk.sdk.domain.entity.UsedeskActionListener;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;

import static ru.usedesk.sdk.utils.LogUtils.LOGD;

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

    public void connect(UsedeskActionListener actionListener, OnMessageListener onMessageListener) {
        if (socket == null) {
            return;
        }

        emitterListeners.add(new BaseEventEmitterListener(actionListener, onMessageListener));
        emitterListeners.add(new ConnectErrorEmitterListener(actionListener, onMessageListener));
        emitterListeners.add(new DisconnectEmitterListener(actionListener, onMessageListener));
        emitterListeners.add(new ConnectEmitterListener(actionListener, onMessageListener));

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
}
