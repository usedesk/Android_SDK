package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response;

import androidx.annotation.NonNull;

import ru.usedesk.chat_sdk.external.entity.UsedeskMessage;

public class NewMessageResponse extends BaseResponse {

    public static final String TYPE = "@@chat/current/ADD_MESSAGE";

    private UsedeskMessage message;

    public NewMessageResponse(@NonNull UsedeskMessage message) {
        super(TYPE);
        this.message = message;
    }

    public UsedeskMessage getMessage() {
        return message;
    }
}