package ru.usedesk.sdk.external.entity.chat;

import com.google.gson.annotations.SerializedName;

public class OfflineForm {

    @SerializedName(Constants.KEY_COMPANY_ID)
    private String companyId;
    private String name;
    private String email;
    private String message;

    public OfflineForm() {
    }

    public OfflineForm(String companyId, String name, String email, String message) {
        this.companyId = companyId;
        this.name = name;
        this.email = email;
        this.message = message;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}