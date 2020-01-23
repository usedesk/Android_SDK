package ru.usedesk.chat_sdk.external.entity;

public class UsedeskClient {

    private String token;
    private String email;
    private int chat;

    public UsedeskClient() {
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getChat() {
        return chat + "";
    }
}