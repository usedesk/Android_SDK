package ru.usedesk.chat_sdk.internal.data.framework.socket.entity;

import ru.usedesk.chat_sdk.internal.domain.entity.BaseMessage;

public class SimpleMessage extends BaseMessage {
    private String payload;

    public String getPayload() {
        return payload;
    }
}
