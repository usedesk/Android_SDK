package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request;

import com.google.gson.annotations.SerializedName;

public class InitChatRequest extends BaseRequest {
    private static final String TYPE = "@@server/chat/INIT";

    private static final String KEY_COMPANY_ID = "company_id";
    private static final String VALUE_CURRENT_SDK = "android";

    @SerializedName(KEY_COMPANY_ID)
    private final String companyId;

    private final String url;
    private final Payload payload;

    public InitChatRequest(String token, String companyId, String url) {
        super(TYPE, token);

        this.companyId = companyId;
        this.url = url;

        payload = new Payload();
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getUrl() {
        return url;
    }

    public Payload getPayload() {
        return payload;
    }

    private static class Payload {

        private String sdk;

        private Payload() {
            this.sdk = VALUE_CURRENT_SDK;
        }

        public String getSdk() {
            return sdk;
        }
    }
}