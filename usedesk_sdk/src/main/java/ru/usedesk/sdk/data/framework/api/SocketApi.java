package ru.usedesk.sdk.data.framework.api;

import android.support.annotation.NonNull;

import org.json.JSONException;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import ru.usedesk.sdk.data.framework.entity.request.BaseRequest;
import ru.usedesk.sdk.domain.entity.Constants;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;

import static ru.usedesk.sdk.utils.LogUtils.LOGD;

public class SocketApi {
    private static final String TAG = SocketApi.class.getSimpleName();

    private Socket socket;

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

    public void connect() {
        if (socket == null) {
            return;
        }

        socket.on(Socket.EVENT_CONNECT, connectEmitterListener);
        socket.on(Socket.EVENT_DISCONNECT, disconnectEmitterListener);
        socket.on(Socket.EVENT_CONNECT_ERROR, connectErrorEmitterListener);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, connectErrorEmitterListener);
        socket.on(Constants.EVENT_SERVER_ACTION, baseEventEmitterListener);

        socket.connect();
    }

    public void disconnect() {
        socket.off(Socket.EVENT_CONNECT, connectEmitterListener);
        socket.off(Socket.EVENT_DISCONNECT, disconnectEmitterListener);
        socket.off(Socket.EVENT_CONNECT_ERROR, connectErrorEmitterListener);
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, connectErrorEmitterListener);
        socket.off(Constants.EVENT_SERVER_ACTION, baseEventEmitterListener);

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
