package ru.usedesk.sdk.data.framework.api.standard.entity.response;

import ru.usedesk.sdk.domain.entity.chat.Message;

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