package ru.usedesk.sample.model.configuration.entity;

import android.support.annotation.Nullable;

public class ConfigurationValidationModel {
    private String companyIdError;
    private String emailError;
    private String urlError;
    private String offlineFormUrlError;
    private String accountIdError;
    private String tokenError;

    public ConfigurationValidationModel() {
    }

    public ConfigurationValidationModel(@Nullable String companyIdError,
                                        @Nullable String emailError,
                                        @Nullable String urlError,
                                        @Nullable String offlineFormUrlError,
                                        @Nullable String accountIdError,
                                        @Nullable String tokenError) {
        this.companyIdError = companyIdError;
        this.emailError = emailError;
        this.urlError = urlError;
        this.offlineFormUrlError = offlineFormUrlError;
        this.accountIdError = accountIdError;
        this.tokenError = tokenError;
    }

    public boolean isSuccessed() {
        return isEmpty(companyIdError)
                && isEmpty(emailError)
                && isEmpty(urlError)
                && isEmpty(offlineFormUrlError)
                && isEmpty(accountIdError)
                && isEmpty(tokenError);
    }

    private boolean isEmpty(@Nullable String value) {
        return value == null || value.isEmpty();
    }

    public String getCompanyIdError() {
        return companyIdError;
    }

    public String getEmailError() {
        return emailError;
    }

    public String getUrlError() {
        return urlError;
    }

    public String getOfflineFormUrlError() {
        return offlineFormUrlError;
    }

    public String getAccountIdError() {
        return accountIdError;
    }

    public String getTokenError() {
        return tokenError;
    }
}
