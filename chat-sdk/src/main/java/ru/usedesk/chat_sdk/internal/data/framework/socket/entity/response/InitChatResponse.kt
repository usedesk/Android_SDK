package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response;

public class InitChatResponse extends BaseResponse {

    public static final String TYPE = "@@chat/current/INITED";

    private String token;
    private Setup setup;
    private Boolean noOperators;

    public InitChatResponse() {
        super(TYPE);
    }

    public String getToken() {
        return token;
    }

    public Setup getSetup() {
        return setup;
    }

    public Boolean getNoOperators() {
        return noOperators;
    }
}