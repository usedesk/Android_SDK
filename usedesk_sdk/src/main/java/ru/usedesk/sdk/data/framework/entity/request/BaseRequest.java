package ru.usedesk.sdk.data.framework.entity.request;

public abstract class BaseRequest {

    private String type;
    private String token;

    public BaseRequest(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}