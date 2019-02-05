package ru.usedesk.sdk.data.framework.api.emitter.listener;


import java.util.Arrays;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import ru.usedesk.sdk.domain.entity.OnMessageListener;
import ru.usedesk.sdk.domain.entity.UsedeskActionListener;

public abstract class EmitterListener implements Emitter.Listener {

    private final UsedeskActionListener actionListener;
    private final OnMessageListener onMessageListener;

    private final List<String> events;

    public EmitterListener(UsedeskActionListener actionListener, OnMessageListener onMessageListener,
                           String... events) {
        this.actionListener = actionListener;
        this.onMessageListener = onMessageListener;
        this.events = Arrays.asList(events);
    }

    public void attachSocket(Socket socket) {
        for (String event : events) {
            socket.on(event, this);
        }
    }

    public void detachSocket(Socket socket) {
        for (String event : events) {
            socket.off(event, this);
        }
    }

    public UsedeskActionListener getActionListener() {
        return actionListener;
    }

    public OnMessageListener getOnMessageListener() {
        return onMessageListener;
    }
}
