package ru.usedesk.sdk.internal.data.framework.api.standard.entity.response;

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