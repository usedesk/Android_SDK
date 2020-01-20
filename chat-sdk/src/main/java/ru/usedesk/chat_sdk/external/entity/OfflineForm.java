package ru.usedesk.chat_sdk.external.entity;

import com.google.gson.annotations.SerializedName;

public class OfflineForm {
    private static final String KEY_COMPANY_ID = "company_id";

    @SerializedName(KEY_COMPANY_ID)
    private final String companyId;
    private final String name;
    private final String email;
    private final String message;

    public OfflineForm(String companyId, String name, String email, String message) {
        this.companyId = companyId;
        this.name = name;
        this.email = email;
        this.message = message;
    }

    public OfflineForm(String name, String email, String message) {
        this(null, name, email, message);
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }
}