package ru.usedesk.sdk.models;

public abstract class BaseResponse {

    private String type;

    public BaseResponse(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}