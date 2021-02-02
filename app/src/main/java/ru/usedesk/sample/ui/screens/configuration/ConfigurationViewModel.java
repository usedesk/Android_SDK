package ru.usedesk.sample.ui.screens.configuration;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.disposables.CompositeDisposable;
import ru.usedesk.common_sdk.entity.UsedeskEvent;
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent;
import ru.usedesk.sample.ServiceLocator;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidation;
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository;
import ru.usedesk.sample.model.configuration.repository.ConfigurationValidator;

public class ConfigurationViewModel extends ViewModel {

    private final ConfigurationRepository configurationRepository;
    private final ConfigurationValidator configurationValidator;

    private final MutableLiveData<Configuration> configuration = new MutableLiveData<>();
    private final MutableLiveData<UsedeskEvent<Object>> goToSdkEvent = new MutableLiveData<>();
    private final MutableLiveData<ConfigurationValidation> configurationValidation = new MutableLiveData<>();

    private final CompositeDisposable disposables = new CompositeDisposable();

    public ConfigurationViewModel() {
        configurationRepository = ServiceLocator.getInstance().getConfigurationRepository();
        configurationValidator = ServiceLocator.getInstance().getConfigurationValidator();
    }

    void onGoSdkClick(@NonNull Configuration configuration) {
        ConfigurationValidation configurationValidation = configurationValidator.validate(configuration);
        if (configurationValidation.isSuccessed()) {
            this.configuration.postValue(configuration);
            configurationRepository.setConfiguration(configuration)
                    .subscribe();
            goToSdkEvent.postValue(new UsedeskSingleLifeEvent<>(null));
        } else {
            this.configurationValidation.postValue(configurationValidation);
        }
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
