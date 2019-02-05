package ru.usedesk.sdk.data.framework.api.emitter.listener;

import io.socket.client.Socket;
import ru.usedesk.sdk.domain.entity.OnMessageListener;
import ru.usedesk.sdk.domain.entity.UsedeskActionListener;

import static ru.usedesk.sdk.utils.LogUtils.LOGE;

public class DisconnectEmitterListener extends EmitterListener {

    private static final String TAG = DisconnectEmitterListener.class.getSimpleName();

    public DisconnectEmitterListener(UsedeskActionListener actionListener,
                                     OnMessageListener onMessageListener) {
        super(actionListener, onMessageListener, Socket.EVENT_DISCONNECT);
    }

    @Override
    public void call(Object... args) {
        LOGE(TAG, "Disconnected.");

        getActionListener().onDisconnected();
    }
}
