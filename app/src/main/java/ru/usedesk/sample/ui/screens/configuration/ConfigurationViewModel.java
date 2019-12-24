package ru.usedesk.sample.ui.screens.configuration;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import ru.usedesk.sample.DI;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidation;
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository;
import ru.usedesk.sample.model.configuration.repository.ConfigurationValidator;
import ru.usedesk.sample.ui._common.Event;
import ru.usedesk.sample.ui._common.OneTimeEvent;

public class ConfigurationViewModel extends ViewModel {

    private final Scheduler workScheduler;

    private final ConfigurationRepository configurationRepository;
    private final ConfigurationValidator configurationValidator;

    private final MutableLiveData<Configuration> configuration = new MutableLiveData<>();
    private final MutableLiveData<Event> goToSdkEvent = new MutableLiveData<>();
    private final MutableLiveData<ConfigurationValidation> configurationValidation = new MutableLiveData<>();

    private final CompositeDisposable disposables = new CompositeDisposable();

    public ConfigurationViewModel() {
        workScheduler = DI.getInstance().getWorkScheduler();
        configurationRepository = DI.getInstance().getConfigurationRepository();
        configurationValidator = DI.getInstance().getConfigurationValidator();

        disposables.add(Single.create((SingleOnSubscribe<Configuration>) emitter -> emitter.onSuccess(configurationRepository.getConfiguration()))
                .subscribeOn(workScheduler)
                .subscribe(configuration::postValue));
    }

    public void onGoSdkClick(@NonNull Configuration configuration) {
        disposables.add(Single.create((SingleOnSubscribe<ConfigurationValidation>) emitter -> emitter.onSuccess(configurationValidator.validate(configuration)))
                .subscribeOn(workScheduler)
                .subscribe(configurationValidation -> {
                    if (configurationValidation.isSuccessed()) {
                        this.configuration.postValue(configuration);
                        configurationRepository.setConfiguration(configuration);
                        goToSdkEvent.postValue(new OneTimeEvent());
                    } else {
                        this.configurationValidation.postValue(configurationValidation);
                    }
                }));
    }

    @NonNull
    public LiveData<Configuration> getConfiguration() {
        return configuration;
    }

    @NonNull
    public LiveData<Event> getGoToSdkEvent() {
        return goToSdkEvent;
    }

    @NonNull
    public LiveData<ConfigurationValidation> getConfigurationValidation() {
        return configurationValidation;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposables.dispose();
    }
}
