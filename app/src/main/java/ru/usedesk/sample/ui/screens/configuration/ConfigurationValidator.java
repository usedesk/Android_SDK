package ru.usedesk.sample.ui.screens.configuration;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.usedesk.sample.App;
import ru.usedesk.sample.R;

public class ConfigurationValidator {

    private final Resources resources;

    public ConfigurationValidator() {
        resources = App.getInstance().getResources();
    }

    public ConfigurationValidationModel validate(@NonNull ConfigurationModel configurationModel) {
        return new ConfigurationValidationModel(
                validateCompanyId(configurationModel),
                validateEmail(configurationModel),
                validateUrl(configurationModel),
                validateOfflineFormUrl(configurationModel),
                validateAccountId(configurationModel),
                validateToken(configurationModel));
    }

    @Nullable
    private String validateCompanyId(@NonNull ConfigurationModel configurationModel) {
        return configurationModel.getToken().isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : null;
    }

    @Nullable
    private String validateEmail(@NonNull ConfigurationModel configurationModel) {
        return configurationModel.getToken().isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : null;
    }

    @Nullable
    private String validateUrl(@NonNull ConfigurationModel configurationModel) {
        return configurationModel.getToken().isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : null;
    }

    @Nullable
    private String validateOfflineFormUrl(@NonNull ConfigurationModel configurationModel) {
        return configurationModel.getToken().isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : null;
    }

    @Nullable
    private String validateAccountId(@NonNull ConfigurationModel configurationModel) {
        return configurationModel.isWithKnowledgeBase() && configurationModel.getAccountId().isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : null;
    }

    @Nullable
    private String validateToken(@NonNull ConfigurationModel configurationModel) {
        return configurationModel.getToken().isEmpty()
                ? resources.getString(R.string.validation_empty_error)
                : null;
    }
}
