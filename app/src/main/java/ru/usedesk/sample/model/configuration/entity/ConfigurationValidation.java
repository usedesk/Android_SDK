package ru.usedesk.sample.model.configuration.entity;

import androidx.annotation.Nullable;

public class ConfigurationValidation {
    private final String companyIdError;
    private final String emailError;
    private final String phoneNumberError;
    private final String urlError;
    private final String offlineFormUrlError;
    private final String accountIdError;
    private final String tokenError;

    public ConfigurationValidation(@Nullable String companyIdError,
                                   @Nullable String emailError,
                                   @Nullable String phoneNumberError,
                                   @Nullable String urlError,
                                   @Nullable String offlineFormUrlError,
                                   @Nullable String accountIdError,
                                   @Nullable String tokenError) {
        this.companyIdError = companyIdError;
        this.emailError = emailError;
        this.phoneNumberError = phoneNumberError;
        this.urlError = urlError;
        this.offlineFormUrlError = offlineFormUrlError;
        this.accountIdError = accountIdError;
        this.tokenError = tokenError;
    }

    public boolean isSuccessed() {
        return isEmpty(companyIdError)
                && isEmpty(emailError)
                && isEmpty(phoneNumberError)
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

    public String getPhoneNumberError() {
        return phoneNumberError;
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
