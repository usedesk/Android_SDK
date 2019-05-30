package ru.usedesk.sdk.external.entity.chat;

public class Client {

    private String token;
    private String email;
    private int chat;

    public Client() {
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