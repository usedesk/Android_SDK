package ru.usedesk.sample.model.configuration.repository;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import ru.usedesk.sample.model.configuration.entity.Configuration;

public class ConfigurationRepository {
    private static final String KEY_URL_CHAT = "urlKey";
    private static final String KEY_URL_OFFLINE_FORM = "offlineFormUrlKey";
    private static final String KEY_URL_TO_SEND_FILE = "urlToSendFileKey";
    private static final String KEY_URL_API = "urlApiKey";
    private static final String KEY_COMPANY_ID = "companyIdKey";
    private static final String KEY_ACCOUNT_ID = "accountIdKey";
    private static final String KEY_TOKEN = "tokenKey";
    private static final String KEY_CLIENT_EMAIL = "emailKey";
    private static final String KEY_CLIENT_NAME = "clientNameKey";
    private static final String KEY_CLIENT_PHONE_NUMBER = "clientPhoneNumberKey";
    private static final String KEY_CLIENT_ADDITIONAL_ID = "clientAdditionalIdKey";
    private static final String KEY_CLIENT_INIT_MESSAGE = "initClientMessageKey";
    private static final String KEY_CUSTOM_AGENT_NAME = "customAgentNameKey";
    private static final String KEY_FOREGROUND_SERVICE = "foregroundServiceKey";
    private static final String KEY_WITH_KNOWLEDGE_BASE = "withKnowledgeBaseKey";

    private final Configuration defaultModel;

    private final SharedPreferences sharedPreferences;
    private final Scheduler workScheduler;
    private Configuration configuration;

    public ConfigurationRepository(@NonNull SharedPreferences sharedPreferences,
                                   @NonNull Scheduler workScheduler) {
        this.sharedPreferences = sharedPreferences;
        this.workScheduler = workScheduler;

        //TODO: Установите свои значения по умолчанию
        defaultModel = new Configuration("https://pubsub.usedesk.ru:1992",
                "https://secure.usedesk.ru/",
                "https://secure.usedesk.ru/uapi/v1/",
                "https://api.usedesk.ru/",
                "153712",
                "4",
                "11eb3f39dec94ecf0fe4a80349903e6ad5ce6d75",
                "android_sdk@usedesk.ru",
                "Иван Иванов",
                "88005553535",
                "777",
                "",
                "",
                false,
                true);
    }

    @NonNull
    public Single<Configuration> getConfiguration() {
        return Single.create((SingleOnSubscribe<Configuration>) emitter -> {
            if (configuration == null) {
                configuration = new Configuration(
                        sharedPreferences.getString(KEY_URL_CHAT, defaultModel.getUrlChat()),
                        sharedPreferences.getString(KEY_URL_OFFLINE_FORM, defaultModel.getUrlOfflineForm()),
                        sharedPreferences.getString(KEY_URL_TO_SEND_FILE, defaultModel.getUrlToSendFile()),
                        sharedPreferences.getString(KEY_URL_API, defaultModel.getUrlApi()),
                        sharedPreferences.getString(KEY_COMPANY_ID, defaultModel.getCompanyId()),
                        sharedPreferences.getString(KEY_ACCOUNT_ID, defaultModel.getAccountId()),
                        sharedPreferences.getString(KEY_TOKEN, defaultModel.getToken()),
                        sharedPreferences.getString(KEY_CLIENT_EMAIL, defaultModel.getClientEmail()),
                        sharedPreferences.getString(KEY_CLIENT_NAME, defaultModel.getClientName()),
                        sharedPreferences.getString(KEY_CLIENT_PHONE_NUMBER, defaultModel.getClientPhoneNumber()),
                        sharedPreferences.getString(KEY_CLIENT_ADDITIONAL_ID, defaultModel.getClientAdditionalId()),
                        sharedPreferences.getString(KEY_CLIENT_INIT_MESSAGE, defaultModel.getClientInitMessage()),
                        sharedPreferences.getString(KEY_CUSTOM_AGENT_NAME, defaultModel.getCustomAgentName()),
                        sharedPreferences.getBoolean(KEY_FOREGROUND_SERVICE, defaultModel.isForegroundService()),
                        sharedPreferences.getBoolean(KEY_WITH_KNOWLEDGE_BASE, defaultModel.isWithKnowledgeBase()));
            }
            emitter.onSuccess(configuration);
        }).subscribeOn(workScheduler);
    }

    @NonNull
    public Completable setConfiguration(@NonNull Configuration configurationModel) {
        this.configuration = configurationModel;
        return Completable.create(emitter -> {
            sharedPreferences.edit()
                    .putString(KEY_URL_CHAT, configurationModel.getUrlChat())
                    .putString(KEY_URL_OFFLINE_FORM, configurationModel.getUrlOfflineForm())
                    .putString(KEY_URL_TO_SEND_FILE, configurationModel.getUrlToSendFile())
                    .putString(KEY_URL_API, configurationModel.getUrlApi())
                    .putString(KEY_COMPANY_ID, configurationModel.getCompanyId())
                    .putString(KEY_ACCOUNT_ID, configurationModel.getAccountId())
                    .putString(KEY_TOKEN, configurationModel.getToken())
                    .putString(KEY_CLIENT_EMAIL, configurationModel.getClientEmail())
                    .putString(KEY_CLIENT_NAME, configurationModel.getClientName())
                    .putString(KEY_CLIENT_PHONE_NUMBER, configurationModel.getClientPhoneNumber())
                    .putString(KEY_CLIENT_ADDITIONAL_ID, configurationModel.getClientAdditionalId())
                    .putString(KEY_CLIENT_INIT_MESSAGE, configurationModel.getClientInitMessage())
                    .putString(KEY_CUSTOM_AGENT_NAME, configurationModel.getCustomAgentName())
                    .putBoolean(KEY_FOREGROUND_SERVICE, configurationModel.isForegroundService())
                    .putBoolean(KEY_WITH_KNOWLEDGE_BASE, configurationModel.isWithKnowledgeBase())
                    .apply();

            emitter.onComplete();
        }).subscribeOn(workScheduler);
    }
}
