package ru.usedesk.sample.ui.screens.configuration;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.disposables.CompositeDisposable;
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration;
import ru.usedesk.common_sdk.entity.UsedeskEvent;
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent;
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration;
import ru.usedesk.sample.ServiceLocator;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidation;
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository;

public class ConfigurationViewModel extends ViewModel {

    private final ConfigurationRepository configurationRepository;

    private final MutableLiveData<Configuration> configuration = new MutableLiveData<>();
    private final MutableLiveData<UsedeskEvent<Object>> goToSdkEvent = new MutableLiveData<>();
    private final MutableLiveData<ConfigurationValidation> configurationValidation = new MutableLiveData<>();

    private final CompositeDisposable disposables = new CompositeDisposable();

    public ConfigurationViewModel() {
        configurationRepository = ServiceLocator.getInstance().getConfigurationRepository();
    }

    void onGoSdkClick(@NonNull Configuration configuration) {
        ConfigurationValidation configurationValidation = validate(configuration);
        this.configurationValidation.postValue(configurationValidation);
        if (configurationValidation.getChatConfigurationValidation().isAllValid()
                && configurationValidation.getKnowledgeBaseConfiguration().isAllValid()) {
            this.configuration.postValue(configuration);
            configurationRepository.setConfiguration(configuration)
                    .subscribe();
            goToSdkEvent.postValue(new UsedeskSingleLifeEvent<>(null));
        }
    }

    @NonNull
    private ConfigurationValidation validate(@NonNull Configuration configuration) {
        UsedeskChatConfiguration.Validation chatValidation = new UsedeskChatConfiguration(
                configuration.getUrlChat(),
                configuration.getUrlOfflineForm(),
                configuration.getUrlToSendFile(),
                configuration.getCompanyId(),
                configuration.getChannelId(),
                configuration.getClientSignature(),
                configuration.getClientEmail(),
                configuration.getClientName(),
                configuration.getClientNote(),
                configuration.getClientPhoneNumber(),
                configuration.getClientAdditionalId(),
                configuration.getClientInitMessage()
        ).validate();

        UsedeskKnowledgeBaseConfiguration.Validation knowledgeBaseValidation = new UsedeskKnowledgeBaseConfiguration(
                configuration.getUrlApi(),
                configuration.getAccountId(),
                configuration.getToken(),
                configuration.getClientEmail(),
                configuration.getClientName()
        ).validate();

        return new ConfigurationValidation(chatValidation,
                knowledgeBaseValidation,
                configuration.getWithKb());
    }

    @NonNull
    public LiveData<Configuration> getConfiguration() {
        disposables.add(configurationRepository.getConfiguration()
                .subscribe(configuration::postValue));

        return configuration;
    }

    @NonNull
    LiveData<UsedeskEvent<Object>> getGoToSdkEvent() {
        return goToSdkEvent;
    }

    @NonNull
    LiveData<ConfigurationValidation> getConfigurationValidation() {
        return configurationValidation;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposables.dispose();
    }

    void setTempConfiguration(@NonNull Configuration configuration) {
        this.configuration.setValue(configuration);
    }
}
