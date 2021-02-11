package ru.usedesk.sample.model.configuration.entity;

import androidx.annotation.Nullable;

public class ConfigurationValidation {
    private final String urlChatError;
    private final String urlOfflineFormError;
    private final String urlToSendFileError;
    private final String urlApiError;
    private final String companyIdError;
    private final String accountIdError;
    private final String tokenError;
    private final String clientEmailError;
    private final String clientPhoneNumberError;

    public ConfigurationValidation(@Nullable String urlChatError,
                                   @Nullable String urlOfflineFormError,
                                   @Nullable String urlToSendFileError,
                                   @Nullable String urlApiError,
                                   @Nullable String companyIdError,
                                   @Nullable String accountIdError,
                                   @Nullable String tokenError,
                                   @Nullable String clientEmailError,
                                   @Nullable String phoneNumberError) {
        this.urlChatError = urlChatError;
        this.urlOfflineFormError = urlOfflineFormError;
        this.urlToSendFileError = urlToSendFileError;
        this.urlApiError = urlApiError;
        this.companyIdError = companyIdError;
        this.accountIdError = accountIdError;
        this.tokenError = tokenError;
        this.clientEmailError = clientEmailError;
        this.clientPhoneNumberError = phoneNumberError;
    }

    public boolean isSuccessed() {
        return isEmpty(urlChatError)
                && isEmpty(urlOfflineFormError)
                && isEmpty(urlToSendFileError)
                && isEmpty(urlApiError)
                && isEmpty(companyIdError)
                && isEmpty(accountIdError)
                && isEmpty(tokenError)
                && isEmpty(clientEmailError)
                && isEmpty(clientPhoneNumberError);
    }

    private boolean isEmpty(@Nullable String value) {
        return value == null || value.isEmpty();
    }

    public String getUrlChatError() {
        return urlChatError;
    }

    public String getUrlOfflineFormError() {
        return urlOfflineFormError;
    }

    public String getUrlToSendFileError() {
        return urlToSendFileError;
    }

    public String getUrlApiError() {
        return urlApiError;
    }

    public String getCompanyIdError() {
        return companyIdError;
    }

    public String getAccountIdError() {
        return accountIdError;
    }

    public String getTokenError() {
        return tokenError;
    }

    public String getClientEmailError() {
        return clientEmailError;
    }

    public String getClientPhoneNumberError() {
        return clientPhoneNumberError;
    }
}
