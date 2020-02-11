package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response;

import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.PayloadMessage;

public class PayloadMessageResponse extends BaseResponse {

    public static final String TYPE = "@@chat/current/ADD_MESSAGE";

    private PayloadMessage message;

    public PayloadMessageResponse() {
        super(TYPE);
    }

    public PayloadMessage getMessage() {
        return message;
    }
}