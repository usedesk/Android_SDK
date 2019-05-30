package ru.usedesk.sdk.external.entity.chat;

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UsedeskConfiguration) {
            UsedeskConfiguration configuration = (UsedeskConfiguration) obj;
            return this.companyId.equals(configuration.companyId) &&
                    this.email.equals(configuration.email) &&
                    this.url.equals(configuration.url);
        }
        return false;
    }
}