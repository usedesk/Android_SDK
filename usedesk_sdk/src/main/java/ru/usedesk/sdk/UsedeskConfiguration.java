package ru.usedesk.sdk;

public class UsedeskConfiguration {

    private String companyId;
    private String email;
    private String url;

    public String getCompanyId() {
        return companyId;
    }

    public String getEmail() {
        return email;
    }

    public String getUrl() {
        return url;
    }

    public static class Builder {

        private UsedeskConfiguration usedeskConfiguration;

        public Builder() {
            usedeskConfiguration = new UsedeskConfiguration();
        }

        public Builder companyId(String companyId) {
            usedeskConfiguration.companyId = companyId;
            return this;
        }

        public Builder email(String email) {
            usedeskConfiguration.email = email;
            return this;
        }

        public Builder url(String url) {
            usedeskConfiguration.url = url;
            return this;
        }

        public UsedeskConfiguration build() {
            if (usedeskConfiguration.companyId == null) {
                throw new NullPointerException("UseDesk.companyId cannot be NULL!");
            }

            if (usedeskConfiguration.email == null) {
                throw new NullPointerException("UseDesk.email cannot be NULL!");
            }

            if (usedeskConfiguration.url == null) {
                throw new NullPointerException("UseDesk.url cannot be NULL!");
            }

            return usedeskConfiguration;
        }
    }
}