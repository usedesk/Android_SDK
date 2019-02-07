package ru.usedesk.sdk.data.framework.entity.request;

public class BaseRequest {

    private String type;
    private String token;

    BaseRequest(String type, String token) {
        this.type = type;
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public String getToken() {
        return token;
    }
}