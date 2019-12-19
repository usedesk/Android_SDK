package ru.usedesk.sample.ui.fragments.configure;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.usedesk.sample.App;

public class ConfigureRepository {
    private static final String COMPANY_ID_KEY = "companyIdKey";
    private static final String EMAIL_KEY = "emailKey";
    private static final String URL_KEY = "urlKey";
    private static final String OFFLINE_FORM_URL_KEY = "offlineFormUrlKey";
    private static final String ACCOUNT_ID_KEY = "accountIdKey";
    private static final String TOKEN_KEY = "tokenKey";
    private static final String CLIENT_NAME_KEY = "clientNameKey";
    private static final String CLIENT_PHONE_NUMBER_KEY = "clientPhoneNumberKey";
    private static final String CLIENT_ADDITIONAL_ID_KEY = "clientAdditionalIdKey";
    private static final String FOREGROUND_SERVICE_KEY = "foregroundServiceKey";
    private static final String CUSTOM_VIEWS_KEY = "customViewsKey";
    private static final String WITH_KNOWLEDGE_BASE_KEY = "withKnowledgeBaseKey";

    private final SharedPreferences sharedPreferences;
    private ConfigureModel configureModel;

    public ConfigureRepository() {
        this.sharedPreferences = App.getInstance()
                .getApplicationContext()
                .getSharedPreferences(getClass().getName(), Context.MODE_PRIVATE);
    }

    private String nullOrToString(@Nullable Object value) {
        return value == null
                ? null
                : value.toString();
    }

    private void put(@NonNull SharedPreferences.Editor editor, @NonNull String key, @Nullable String value) {
        editor.putString(key, value);
    }

    private void put(@NonNull SharedPreferences.Editor editor, @NonNull String key, @Nullable Long value) {
        put(editor, key, nullOrToString(value));
    }

    private void put(@NonNull SharedPreferences.Editor editor, @NonNull String key, @Nullable Boolean value) {
        put(editor, key, nullOrToString(value));
    }

    @Nullable
    private String getString(@NonNull SharedPreferences sharedPreferences, @NonNull String key) {
        return sharedPreferences.getString(key, null);
    }

    @Nullable
    private Long getLong(@NonNull SharedPreferences sharedPreferences, @NonNull String key) {
        String value = sharedPreferences.getString(key, null);
        return value == null
                ? null
                : Long.valueOf(value);
    }

    @Nullable
    private Boolean getBool(@NonNull SharedPreferences sharedPreferences, @NonNull String key) {
        String value = sharedPreferences.getString(key, null);
        return value == null
                ? null
                : Boolean.valueOf(value);
    }

    @NonNull
    public ConfigureModel getConfigureModel() {
        if (configureModel == null) {
            configureModel = new ConfigureModel.Builder(null)
                    .setCompanyId(getLong(sharedPreferences, COMPANY_ID_KEY))
                    .setEmail(getString(sharedPreferences, EMAIL_KEY))
                    .setUrl(getString(sharedPreferences, URL_KEY))
                    .setOfflineFormUrl(getString(sharedPreferences, OFFLINE_FORM_URL_KEY))
                    .setAccountId(getLong(sharedPreferences, ACCOUNT_ID_KEY))
                    .setToken(getString(sharedPreferences, TOKEN_KEY))
                    .setClientName(getString(sharedPreferences, CLIENT_NAME_KEY))
                    .setClientPhoneNumber(getString(sharedPreferences, CLIENT_PHONE_NUMBER_KEY))
                    .setClientAdditionalId(getString(sharedPreferences, CLIENT_ADDITIONAL_ID_KEY))
                    .setForegroundService(getBool(sharedPreferences, FOREGROUND_SERVICE_KEY))
                    .setCustomViews(getBool(sharedPreferences, CUSTOM_VIEWS_KEY))
                    .setWithKnowledgeBase(getBool(sharedPreferences, WITH_KNOWLEDGE_BASE_KEY))
                    .build();
        }
        return configureModel;
    }

    public void setConfigureModel(@NonNull ConfigureModel configureModel) {
        this.configureModel = configureModel;

        SharedPreferences.Editor editor = sharedPreferences.edit();

        put(editor, COMPANY_ID_KEY, configureModel.getCompanyId());
        put(editor, EMAIL_KEY, configureModel.getEmail());
        put(editor, URL_KEY, configureModel.getUrl());
        put(editor, OFFLINE_FORM_URL_KEY, configureModel.getOfflineFormUrl());
        put(editor, ACCOUNT_ID_KEY, configureModel.getAccountId());
        put(editor, TOKEN_KEY, configureModel.getToken());
        put(editor, CLIENT_NAME_KEY, configureModel.getClientName());
        put(editor, CLIENT_PHONE_NUMBER_KEY, configureModel.getClientPhoneNumber());
        put(editor, CLIENT_ADDITIONAL_ID_KEY, configureModel.getClientAdditionalId());
        put(editor, FOREGROUND_SERVICE_KEY, configureModel.getForegroundService());
        put(editor, CUSTOM_VIEWS_KEY, configureModel.getCustomViews());
        put(editor, WITH_KNOWLEDGE_BASE_KEY, configureModel.getWithKnowledgeBase());

        editor.apply();
    }
}
