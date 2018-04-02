package ru.usedesk.sdk.models;

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