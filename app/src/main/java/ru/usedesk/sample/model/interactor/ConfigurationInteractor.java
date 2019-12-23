package ru.usedesk.sample.model.interactor;

import android.support.annotation.NonNull;

import io.reactivex.disposables.CompositeDisposable;
import ru.usedesk.sample.DI;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.model.configuration.entity.ConfigurationModelo;
import ru.usedesk.sample.model.configuration.entity.ConfigurationModelo.DataKey;
import ru.usedesk.sample.model.configuration.entity.ConfigurationModelo.IntentKey;
import ru.usedesk.sample.model.configuration.entity.SingleLiveEvent;
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository;
import ru.usedesk.sample.model.configuration.repository.ConfigurationValidator;

public class ConfigurationInteractor {
    private final ConfigurationModelo configurationModelo = new ConfigurationModelo();
    private final CompositeDisposable intentDisposables = new CompositeDisposable();

    private final ConfigurationRepository configurationRepository;
    private final ConfigurationValidator configurationValidator;

    public ConfigurationInteractor() {
        configurationRepository = DI.getInstance().getConfigurationRepository();
        configurationValidator = DI.getInstance().getConfigurationValidator();

        justTransfer(IntentKey.EDIT_COMPANY_ID, DataKey.COMPANY_ID);
        justTransfer(IntentKey.EDIT_EMAIL, DataKey.EMAIL);
        justTransfer(IntentKey.EDIT_URL, DataKey.URL);
        justTransfer(IntentKey.EDIT_OFFLINE_FORM_URL, DataKey.OFFLINE_FORM_URL);
        justTransfer(IntentKey.EDIT_ACCOUNT_ID, DataKey.ACCOUNT_ID);
        justTransfer(IntentKey.EDIT_TOKEN, DataKey.TOKEN);
        justTransfer(IntentKey.EDIT_CLIENT_NAME, DataKey.CLIENT_PHONE_NUMBER);
        justTransfer(IntentKey.EDIT_CLIENT_PHONE_NUMBER, DataKey.CLIENT_PHONE_NUMBER);
        justTransfer(IntentKey.EDIT_CLIENT_ADDITIONAL_ID, DataKey.CLIENT_ADDITIONAL_ID);
        justTransfer(IntentKey.SET_FOREGROUND_SERVICE, DataKey.FOREGROUND_SERVICE);
        justTransfer(IntentKey.SET_CUSTOM_VIEWS, DataKey.CUSTOM_VIEWS);
        justTransfer(IntentKey.SET_WITH_KNOWLEDGE_BASE, DataKey.WITH_KNOWLEDGE_BASE);

        intentDisposables.add(configurationModelo.getIntentObservable(IntentKey.EVENT_INIT_CONFIGURATION)
                .ignoreElements()
                .subscribe(() -> {
                    Configuration configuration = configurationRepository.getConfiguration();
                    configurationModelo.setData(DataKey.COMPANY_ID, configuration.getCompanyId());
                    configurationModelo.setData(DataKey.EMAIL, configuration.getEmail());
                    configurationModelo.setData(DataKey.URL, configuration.getUrl());
                    configurationModelo.setData(DataKey.OFFLINE_FORM_URL, configuration.getOfflineFormUrl());
                    configurationModelo.setData(DataKey.ACCOUNT_ID, configuration.getAccountId());
                    configurationModelo.setData(DataKey.TOKEN, configuration.getToken());
                    configurationModelo.setData(DataKey.CLIENT_NAME, configuration.getClientName());
                    configurationModelo.setData(DataKey.CLIENT_PHONE_NUMBER, configuration.getClientPhoneNumber());
                    configurationModelo.setData(DataKey.CLIENT_ADDITIONAL_ID, configuration.getClientAdditionalId());
                    configurationModelo.setData(DataKey.FOREGROUND_SERVICE, configuration.isForegroundService());
                    configurationModelo.setData(DataKey.CUSTOM_VIEWS, configuration.isCustomViews());
                    configurationModelo.setData(DataKey.WITH_KNOWLEDGE_BASE, configuration.isWithKnowledgeBase());
                }));

        intentDisposables.add(configurationModelo.getIntentObservable(IntentKey.EVENT_SET_CONFIGURATION)
                .ignoreElements()
                .subscribe(() -> {
                    Boolean foregroundService = configurationModelo.getData(DataKey.FOREGROUND_SERVICE);
                    Boolean customViews = configurationModelo.getData(DataKey.CUSTOM_VIEWS);
                    Boolean withKnowledgeBase = configurationModelo.getData(DataKey.WITH_KNOWLEDGE_BASE);

                    String companyId = configurationModelo.getData(DataKey.COMPANY_ID);
                    String companyIdError = configurationValidator.validateCompanyId(companyId);
                    configurationModelo.setData(DataKey.COMPANY_ID_ERROR, companyIdError);

                    String email = configurationModelo.getData(DataKey.EMAIL);
                    String emailError = configurationValidator.validateEmail(email);
                    configurationModelo.setData(DataKey.EMAIL_ERROR, emailError);

                    String url = configurationModelo.getData(DataKey.URL);
                    String urlError = configurationValidator.validateUrl(url);
                    configurationModelo.setData(DataKey.URL_ERROR, urlError);

                    String offlineFormUrl = configurationModelo.getData(DataKey.OFFLINE_FORM_URL);
                    String offlineFormUrlError = configurationValidator.validateOfflineFormUrl(offlineFormUrl);
                    configurationModelo.setData(DataKey.OFFLINE_FORM_URL_ERROR, offlineFormUrlError);

                    String accountId = configurationModelo.getData(DataKey.ACCOUNT_ID);
                    String accountIdError = configurationValidator.validateAccountId(accountId, withKnowledgeBase);
                    configurationModelo.setData(DataKey.ACCOUNT_ID_ERROR, accountIdError);

                    String token = configurationModelo.getData(DataKey.TOKEN);
                    String tokenError = configurationValidator.validateToken(token, withKnowledgeBase);
                    configurationModelo.setData(DataKey.TOKEN_ERROR, tokenError);

                    String clientName = configurationModelo.getData(DataKey.CLIENT_NAME);
                    String clientPhoneNumber = configurationModelo.getData(DataKey.CLIENT_PHONE_NUMBER);
                    String clientAdditionalId = configurationModelo.getData(DataKey.CLIENT_ADDITIONAL_ID);

                    if (isAllNotEmpty(companyIdError, emailError, urlError, offlineFormUrlError, accountIdError, tokenError)) {
                        configurationRepository.setConfiguration(new Configuration(companyId, email, url, offlineFormUrl, accountId, token,
                                clientName, clientPhoneNumber, clientAdditionalId, foregroundService, customViews, withKnowledgeBase));

                        configurationModelo.setData(DataKey.EVENT_SET_CONFIGURATION, new SingleLiveEvent());
                    }
                }));

        configurationModelo.setIntent(IntentKey.EVENT_INIT_CONFIGURATION, "");
    }

    @NonNull
    public ConfigurationModelo getConfigurationModelo() {
        return configurationModelo;
    }

    private boolean isAllNotEmpty(@NonNull String... values) {
        for (String value : values) {
            if (!value.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void justTransfer(@NonNull IntentKey intentKey, @NonNull DataKey dataKey) {
        intentDisposables.add(configurationModelo.getIntentObservable(intentKey)
                .subscribe(data -> configurationModelo.setData(dataKey, data)));
    }
}
