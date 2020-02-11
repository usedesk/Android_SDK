package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response;

import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.SimpleMessage;

public class SimpleMessageResponse extends BaseResponse {

    public static final String TYPE = "@@chat/current/ADD_MESSAGE";

    private SimpleMessage message;

    public SimpleMessageResponse() {
        super(TYPE);
    }

    public SimpleMessage getMessage() {
        return message;
    }
}