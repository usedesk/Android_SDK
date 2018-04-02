package ru.usedesk.sdk.models;

public class SetEmailRequest extends BaseRequest {

    public static final String TYPE = "@@server/chat/SET_EMAIL";

    private String email;

    public SetEmailRequest() {
        super(TYPE);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}