package ru.usedesk.sdk.domain.entity;

public class UsedeskConfiguration {

    private String companyId;
    private String email;
    private String url;

    public UsedeskConfiguration(String companyId, String email, String url) {
        this.companyId = companyId;
        this.email = email;
        this.url = url;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getEmail() {
        return email;
    }

    public String getUrl() {
        return url;
    }
}