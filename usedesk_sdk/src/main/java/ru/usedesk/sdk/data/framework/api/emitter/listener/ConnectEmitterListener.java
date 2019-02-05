package ru.usedesk.sdk.data.framework.api.emitter.listener;

import java.util.Arrays;

import io.socket.client.Socket;
import ru.usedesk.sdk.domain.entity.OnMessageListener;
import ru.usedesk.sdk.domain.entity.UsedeskActionListener;

import static ru.usedesk.sdk.utils.LogUtils.LOGD;

public class ConnectEmitterListener extends EmitterListener {

    private static final String TAG = ConnectEmitterListener.class.getSimpleName();

    public ConnectEmitterListener(UsedeskActionListener actionListener,
                                  OnMessageListener onMessageListener) {
        super(actionListener, onMessageListener, Socket.EVENT_CONNECT);
    }

    @Override
    public void call(Object... args) {
        LOGD(TAG, "ConnectEmitterListener.args = " + Arrays.toString(args));
        getOnMessageListener().onInitChat();
    }
}
