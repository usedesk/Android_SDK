package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response;

import ru.usedesk.chat_sdk.external.entity.Message;

public class NewMessageResponse extends BaseResponse {

    public static final String TYPE = "@@chat/current/ADD_MESSAGE";

    private Message message;

    public NewMessageResponse() {
        super(TYPE);
    }

    public Message getMessage() {
        return message;
    }
}