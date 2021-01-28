package ru.usedesk.sample.model.configuration.repository;

import android.content.res.Resources;

import androidx.annotation.NonNull;

import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil;
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
        return !UsedeskValidatorUtil.isValidEmailNecessary(email)
                ? resources.getString(R.string.validation_email_error)
                : "";
    }

    @NonNull
    private String validatePhoneNumber(@NonNull String phoneNumber) {
        return !UsedeskValidatorUtil.isValidPhone(phoneNumber)
                ? resources.getString(R.string.validation_phone_error)
                : "";
    }

    @NonNull
    private String validateUrlChat(@NonNull String urlChat) {
        return !UsedeskValidatorUtil.isValidUrlNecessary(urlChat)
                ? resources.getString(R.string.validation_url_error)
                : "";
    }

    @NonNull
    private String validateUrlOfflineForm(@NonNull String offlineFormUrl) {
        return !UsedeskValidatorUtil.isValidUrl(offlineFormUrl)
                ? resources.getString(R.string.validation_url_error)
                : "";
    }

    @NonNull
    private String validateUrlToSendFile(@NonNull String urlToSendFile) {
        return !UsedeskValidatorUtil.isValidUrl(urlToSendFile)
                ? resources.getString(R.string.validation_url_error)
                : "";
    }

    @NonNull
    private String validateUrlApi(@NonNull String urlApi) {
        return !UsedeskValidatorUtil.isValidUrl(urlApi)
                ? resources.getString(R.string.validation_url_error)
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
        return new ConfigurationValidation(validateUrlChat(configuration.getUrlChat()),
                validateUrlOfflineForm(configuration.getUrlOfflineForm()),
                validateUrlToSendFile(configuration.getUrlToSendFile()),
                validateUrlApi(configuration.getUrlApi()),
                validateCompanyId(configuration.getCompanyId()),
                validateAccountId(configuration.getAccountId(), configuration.isWithKnowledgeBase()),
                validateToken(configuration.getToken(), configuration.isWithKnowledgeBase()),
                validateEmail(configuration.getClientEmail()),
                validatePhoneNumber(configuration.getClientPhoneNumber()));
    }
}
