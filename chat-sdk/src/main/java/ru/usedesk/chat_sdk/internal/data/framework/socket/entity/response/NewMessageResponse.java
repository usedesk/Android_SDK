package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response;

import ru.usedesk.chat_sdk.external.entity.UsedeskMessage;

public class NewMessageResponse extends BaseResponse {

    public static final String TYPE = "@@chat/current/ADD_MESSAGE";

    private UsedeskMessage message;

    public NewMessageResponse() {
        super(TYPE);
    }

    public UsedeskMessage getMessage() {
        return message;
    }
}