package ru.usedesk.sdk.models;

import com.google.gson.annotations.SerializedName;

import static ru.usedesk.sdk.Constants.KEY_COMPANY_ID;
import static ru.usedesk.sdk.Constants.VALUE_CURRENT_SDK;

public class InitChatRequest extends BaseRequest {

    public static final String TYPE = "@@server/chat/INIT";

    @SerializedName(KEY_COMPANY_ID)
    private String companyId;

    private String url;
    private Payload payload;

    public InitChatRequest() {
        super(TYPE);
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
            this.sdk = VALUE_CURRENT_SDK;
        }

        public String getSdk() {
            return sdk;
        }
    }
}