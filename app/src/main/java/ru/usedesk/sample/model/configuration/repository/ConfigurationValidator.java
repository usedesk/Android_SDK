package ru.usedesk.sample.model.configuration.repository;

import android.content.res.Resources;

import androidx.annotation.NonNull;

import ru.usedesk.common_sdk.external.entity.exceptions.Validators;
import ru.usedesk.sample.R;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidation;

public class ConfigurationValidator {

    private final Resources resources;

    public ConfigurationValidator(Resources resources) {
        this.resources = resources;
    }

    @NonNull
    private String validateCompanyId(@NonNull String companyId) {
        return companyId.isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : "";
    }

    @NonNull
    private String validateEmail(@NonNull String email) {
        return email.isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : "";
    }

    @NonNull
    private String validatePhoneNumber(@NonNull String phoneNumber) {
        return !Validators.isValidPhonePhone(phoneNumber)
                ? resources.getString(R.string.validation_empty_error)
                : "";
    }

    @NonNull
    private String validateUrl(@NonNull String url) {
        return !Validators.isValidUrlNecessary(url)
                ? resources.getString(R.string.validation_empty_error)
                : "";
    }

    @NonNull
    private String validateOfflineFormUrl(@NonNull String offlineFormUrl) {
        return !Validators.isValidUrl(offlineFormUrl)
                ? resources.getString(R.string.validation_empty_error)
                : "";
    }

    @NonNull
    private String validateAccountId(@NonNull String accountId, @NonNull Boolean withKnowledgeBase) {
        return withKnowledgeBase && accountId.isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : "";
    }

    @NonNull
    private String validateToken(@NonNull String token, @NonNull Boolean withKnowledgeBase) {
        return withKnowledgeBase && token.isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : "";
    }

    @NonNull
    public ConfigurationValidation validate(@NonNull Configuration configuration) {
        return new ConfigurationValidation(validateCompanyId(configuration.getCompanyId()),
                validateEmail(configuration.getEmail()),
                validatePhoneNumber(configuration.getClientPhoneNumber()),
                validateUrl(configuration.getUrl()),
                validateOfflineFormUrl(configuration.getOfflineFormUrl()),
                validateAccountId(configuration.getAccountId(), configuration.isWithKnowledgeBase()),
                validateToken(configuration.getToken(), configuration.isWithKnowledgeBase()));
    }
}
