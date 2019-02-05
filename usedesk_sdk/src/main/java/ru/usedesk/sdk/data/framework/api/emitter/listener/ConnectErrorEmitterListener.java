package ru.usedesk.sdk.data.framework.api.emitter.listener;

import java.util.Arrays;

import io.socket.client.Socket;
import ru.usedesk.sdk.R;
import ru.usedesk.sdk.domain.entity.OnMessageListener;
import ru.usedesk.sdk.domain.entity.UsedeskActionListener;

import static ru.usedesk.sdk.utils.LogUtils.LOGE;

public class ConnectErrorEmitterListener extends EmitterListener {

    private static final String TAG = ConnectEmitterListener.class.getSimpleName();

    public ConnectErrorEmitterListener(UsedeskActionListener actionListener,
                                       OnMessageListener onMessageListener) {
        super(actionListener, onMessageListener, Socket.EVENT_CONNECT_ERROR,
                Socket.EVENT_CONNECT_TIMEOUT);
    }

    @Override
    public void call(Object... args) {
        LOGE(TAG, "Error connecting: + " + Arrays.toString(args));

        getActionListener().onError(R.string.message_connecting_error);
    }
}
