package ru.usedesk.sample.model.configuration.repository;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import ru.usedesk.sample.R;

public class ConfigurationValidator {

    private final Resources resources;

    public ConfigurationValidator(Resources resources) {
        this.resources = resources;
    }

    @NonNull
    public String validateCompanyId(@NonNull String companyId) {
        return companyId.isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : "";
    }

    @NonNull
    public String validateEmail(@NonNull String email) {
        return email.isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : "";
    }

    @NonNull
    public String validateUrl(@NonNull String url) {
        return url.isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : "";
    }

    @NonNull
    public String validateOfflineFormUrl(@NonNull String offlineFormUrl) {
        return offlineFormUrl.isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : "";
    }

    @NonNull
    public String validateAccountId(@NonNull String accountId, @NonNull Boolean withKnowledgeBase) {
        return withKnowledgeBase && accountId.isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : "";
    }

    @NonNull
    public String validateToken(@NonNull String token, @NonNull Boolean withKnowledgeBase) {
        return withKnowledgeBase && token.isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : "";
    }
}
