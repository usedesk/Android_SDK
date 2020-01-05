package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response;

public abstract class BaseResponse {

    private String type;

    public BaseResponse(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}