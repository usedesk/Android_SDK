package ru.usedesk.sdk.internal.data.framework.api.standard.entity.response;

public abstract class BaseResponse {

    private String type;

    public BaseResponse(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}