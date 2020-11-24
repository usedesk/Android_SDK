package ru.usedesk.chat_sdk.internal.data.framework.socket.entity;

import ru.usedesk.chat_sdk.external.entity.UsedeskPayload;
import ru.usedesk.chat_sdk.internal.domain.entity.BaseMessage;

public class PayloadMessage extends BaseMessage {
    private UsedeskPayload payload;

    public UsedeskPayload getPayload() {
        return payload;
    }
}