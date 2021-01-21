package ru.usedesk.sample.model.configuration.repository;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import ru.usedesk.sample.model.configuration.entity.Configuration;

public class ConfigurationRepository {
    private static final String COMPANY_ID_KEY = "companyIdKey";
    private static final String EMAIL_KEY = "emailKey";
    private static final String SOCKET_URL_KEY = "urlKey";
    private static final String API_KEY = "offlineFormUrlKey";
    private static final String ACCOUNT_ID_KEY = "accountIdKey";
    private static final String TOKEN_KEY = "tokenKey";
    private static final String CLIENT_NAME_KEY = "clientNameKey";
    private static final String CLIENT_PHONE_NUMBER_KEY = "clientPhoneNumberKey";
    private static final String CLIENT_ADDITIONAL_ID_KEY = "clientAdditionalIdKey";
    private static final String INIT_CLIENT_MESSAGE_KEY = "initClientMessageKey";
    private static final String CUSTOM_AGENT_NAME_KEY = "customAgentNameKey";
    private static final String FOREGROUND_SERVICE_KEY = "foregroundServiceKey";
    private static final String CUSTOM_VIEWS_KEY = "customViewsKey";
    private static final String WITH_KNOWLEDGE_BASE_KEY = "withKnowledgeBaseKey";

    private final Configuration defaultModel;

    private final SharedPreferences sharedPreferences;
    private final Scheduler workScheduler;
    private Configuration configuration;

    public ConfigurationRepository(@NonNull SharedPreferences sharedPreferences,
                                   @NonNull Scheduler workScheduler) {
        this.sharedPreferences = sharedPreferences;
        this.workScheduler = workScheduler;

        //TODO: Установите свои значения по умолчанию
        defaultModel = new Configuration("153712",
                "android_sdk@usedesk.ru",
                "https://pubsub.usedesk.ru:1992",
                "https://secure.usedesk.ru",
                "4",
                "11eb3f39dec94ecf0fe4a80349903e6ad5ce6d75",
                "Иван Иванов",
                "88005553535",
                "777",
                "",
                "",
                false,
                false,
                true);
    }

    @NonNull
    public Single<Configuration> getConfiguration() {
        return Single.create((SingleOnSubscribe<Configuration>) emitter -> {
            if (configuration == null) {
                configuration = new Configuration(sharedPreferences.getString(COMPANY_ID_KEY, defaultModel.getCompanyId()),
                        sharedPreferences.getString(EMAIL_KEY, defaultModel.getEmail()),
                        sharedPreferences.getString(SOCKET_URL_KEY, defaultModel.getSocketUrl()),
                        sharedPreferences.getString(API_KEY, defaultModel.getSecureUrl()),
                        sharedPreferences.getString(ACCOUNT_ID_KEY, defaultModel.getAccountId()),
                        sharedPreferences.getString(TOKEN_KEY, defaultModel.getToken()),
                        sharedPreferences.getString(CLIENT_NAME_KEY, defaultModel.getClientName()),
                        sharedPreferences.getString(CLIENT_PHONE_NUMBER_KEY, defaultModel.getClientPhoneNumber()),
                        sharedPreferences.getString(CLIENT_ADDITIONAL_ID_KEY, defaultModel.getClientAdditionalId()),
                        sharedPreferences.getString(INIT_CLIENT_MESSAGE_KEY, defaultModel.getInitClientMessage()),
                        sharedPreferences.getString(CUSTOM_AGENT_NAME_KEY, defaultModel.getCustomAgentName()),
                        sharedPreferences.getBoolean(FOREGROUND_SERVICE_KEY, defaultModel.isForegroundService()),
                        sharedPreferences.getBoolean(CUSTOM_VIEWS_KEY, defaultModel.isCustomViews()),
                        sharedPreferences.getBoolean(WITH_KNOWLEDGE_BASE_KEY, defaultModel.isWithKnowledgeBase()));
            }
            emitter.onSuccess(configuration);
        }).subscribeOn(workScheduler);
    }

    @NonNull
    public Completable setConfiguration(@NonNull Configuration configurationModel) {
        this.configuration = configurationModel;
        return Completable.create(emitter -> {
            sharedPreferences.edit()
                    .putString(COMPANY_ID_KEY, configurationModel.getCompanyId())
                    .putString(EMAIL_KEY, configurationModel.getEmail())
                    .putString(SOCKET_URL_KEY, configurationModel.getSocketUrl())
                    .putString(API_KEY, configurationModel.getSecureUrl())
                    .putString(ACCOUNT_ID_KEY, configurationModel.getAccountId())
                    .putString(TOKEN_KEY, configurationModel.getToken())
                    .putString(CLIENT_NAME_KEY, configurationModel.getClientName())
                    .putString(CLIENT_PHONE_NUMBER_KEY, configurationModel.getClientPhoneNumber())
                    .putString(CLIENT_ADDITIONAL_ID_KEY, configurationModel.getClientAdditionalId())
                    .putString(INIT_CLIENT_MESSAGE_KEY, configurationModel.getInitClientMessage())
                    .putString(CUSTOM_AGENT_NAME_KEY, configurationModel.getCustomAgentName())
                    .putBoolean(FOREGROUND_SERVICE_KEY, configurationModel.isForegroundService())
                    .putBoolean(CUSTOM_VIEWS_KEY, configurationModel.isCustomViews())
                    .putBoolean(WITH_KNOWLEDGE_BASE_KEY, configurationModel.isWithKnowledgeBase())
                    .apply();

            emitter.onComplete();
        }).subscribeOn(workScheduler);
    }
}
