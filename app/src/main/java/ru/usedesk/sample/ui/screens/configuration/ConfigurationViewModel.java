package ru.usedesk.sample.ui.screens.configuration;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import ru.usedesk.sample.model.configuration.entity.ConfigurationModel;
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidationModel;
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository;
import ru.usedesk.sample.model.configuration.repository.ConfigurationValidator;

public class ConfigurationViewModel extends ViewModel {

    private final ConfigurationRepository configurationRepository;
    private final ConfigurationValidator configurationValidator;

    private final MutableLiveData<ConfigurationModel> configureLiveData = new MutableLiveData<>();
    private final MutableLiveData<ConfigurationValidationModel> configureValidationLiveData = new MutableLiveData<>();

    public ConfigurationViewModel() {
        configurationRepository = new ConfigurationRepository();
        configurationValidator = new ConfigurationValidator();

        configureLiveData.postValue(configurationRepository.getConfigurationModel());
    }

    @NonNull
    public LiveData<ConfigurationModel> getConfigureModule() {
        return configureLiveData;
    }

    @NonNull
    public LiveData<ConfigurationValidationModel> getConfigureValidationLiveData() {
        return configureValidationLiveData;
    }

    public void onGoToSdk(@NonNull String companyId,
                          @NonNull String email,
                          @NonNull String url,
                          @NonNull String offlineFormUrl,
                          @NonNull String accountId,
                          @NonNull String token,
                          @NonNull String clientName,
                          @NonNull String clientPhoneNumber,
                          @NonNull String clientAdditionalId,
                          @NonNull Boolean foregroundService,
                          @NonNull Boolean customViews,
                          @NonNull Boolean withKnowledgeBase) {
        ConfigurationModel configurationModel = new ConfigurationModel.Builder(null)
                .setCompanyId(companyId)
                .setEmail(email)
                .setUrl(url)
                .setOfflineFormUrl(offlineFormUrl)
                .setAccountId(accountId)
                .setToken(token)
                .setClientName(clientName)
                .setClientPhoneNumber(clientPhoneNumber)
                .setClientAdditionalId(clientAdditionalId)
                .setForegroundService(foregroundService)
                .setCustomViews(customViews)
                .setWithKnowledgeBase(withKnowledgeBase)
                .build();

        ConfigurationValidationModel validationModel = configurationValidator.validate(configurationModel);
        configureValidationLiveData.postValue(validationModel);
        if (validationModel.isSuccessed()) {
            configurationRepository.setConfigurationModel(configurationModel);
        }
    }
}
