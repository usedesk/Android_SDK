package ru.usedesk.sample.ui.screens.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import ru.usedesk.sample.App;

public class ConfigurationRepository {
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

    private final ConfigurationModel defaultModel;

    private final SharedPreferences sharedPreferences;
    private ConfigurationModel configurationModel;

    public ConfigurationRepository() {
        this.sharedPreferences = App.getInstance()
                .getApplicationContext()
                .getSharedPreferences(getClass().getName(), Context.MODE_PRIVATE);

        //TODO: Установите свои значения по умолчанию
        defaultModel = new ConfigurationModel.Builder(null)
                .setCompanyId("153712")
                .setEmail("android_sdk@usedesk.ru")
                .setUrl("https://pubsub.usedesk.ru:1992")
                .setOfflineFormUrl("https://secure.usedesk.ru")
                .setAccountId("4")
                .setToken("11eb3f39dec94ecf0fe4a80349903e6ad5ce6d75")
                .setClientName("Иван Иванов")
                .setClientPhoneNumber("88005553535")
                .setClientAdditionalId("777")
                .setForegroundService(false)
                .setCustomViews(false)
                .setWithKnowledgeBase(true)
                .build();
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    public ConfigurationModel getConfigurationModel() {
        if (configurationModel == null) {
            configurationModel = new ConfigurationModel.Builder(null)
                    .setCompanyId(sharedPreferences.getString(COMPANY_ID_KEY, defaultModel.getCompanyId()))
                    .setEmail(sharedPreferences.getString(EMAIL_KEY, defaultModel.getEmail()))
                    .setUrl(sharedPreferences.getString(URL_KEY, defaultModel.getUrl()))
                    .setOfflineFormUrl(sharedPreferences.getString(OFFLINE_FORM_URL_KEY, defaultModel.getOfflineFormUrl()))
                    .setAccountId(sharedPreferences.getString(ACCOUNT_ID_KEY, defaultModel.getAccountId()))
                    .setToken(sharedPreferences.getString(TOKEN_KEY, defaultModel.getToken()))
                    .setClientName(sharedPreferences.getString(CLIENT_NAME_KEY, defaultModel.getClientName()))
                    .setClientPhoneNumber(sharedPreferences.getString(CLIENT_PHONE_NUMBER_KEY, defaultModel.getClientPhoneNumber()))
                    .setClientAdditionalId(sharedPreferences.getString(CLIENT_ADDITIONAL_ID_KEY, defaultModel.getClientAdditionalId()))
                    .setForegroundService(sharedPreferences.getBoolean(FOREGROUND_SERVICE_KEY, defaultModel.isForegroundService()))
                    .setCustomViews(sharedPreferences.getBoolean(CUSTOM_VIEWS_KEY, defaultModel.isCustomViews()))
                    .setWithKnowledgeBase(sharedPreferences.getBoolean(WITH_KNOWLEDGE_BASE_KEY, defaultModel.isWithKnowledgeBase()))
                    .build();
        }
        return configurationModel;
    }

    public void setConfigurationModel(@NonNull ConfigurationModel configurationModel) {
        this.configurationModel = configurationModel;

        sharedPreferences.edit()
                .putString(COMPANY_ID_KEY, configurationModel.getCompanyId())
                .putString(EMAIL_KEY, configurationModel.getEmail())
                .putString(URL_KEY, configurationModel.getUrl())
                .putString(OFFLINE_FORM_URL_KEY, configurationModel.getOfflineFormUrl())
                .putString(ACCOUNT_ID_KEY, configurationModel.getAccountId())
                .putString(TOKEN_KEY, configurationModel.getToken())
                .putString(CLIENT_NAME_KEY, configurationModel.getClientName())
                .putString(CLIENT_PHONE_NUMBER_KEY, configurationModel.getClientPhoneNumber())
                .putString(CLIENT_ADDITIONAL_ID_KEY, configurationModel.getClientAdditionalId())
                .putBoolean(FOREGROUND_SERVICE_KEY, configurationModel.isForegroundService())
                .putBoolean(CUSTOM_VIEWS_KEY, configurationModel.isCustomViews())
                .putBoolean(WITH_KNOWLEDGE_BASE_KEY, configurationModel.isWithKnowledgeBase())
                .apply();
    }
}
