package ru.usedesk.sdk.data.framework.api.standard.entity.request;

import com.google.gson.annotations.SerializedName;

import ru.usedesk.sdk.domain.entity.chat.Constants;

public class InitChatRequest extends BaseRequest {

    public static final String TYPE = "@@server/chat/INIT";

    @SerializedName(Constants.KEY_COMPANY_ID)
    private String companyId;

    private String url;
    private Payload payload;

    public InitChatRequest(String token, String companyId, String url) {
        super(TYPE, token);

        this.companyId = companyId;
        this.url = url;

        payload = new Payload();
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Payload getPayload() {
        return payload;
    }

    private class Payload {

        private String sdk;

        private Payload() {
            this.sdk = Constants.VALUE_CURRENT_SDK;
        }

        public String getSdk() {
            return sdk;
        }
    }
}