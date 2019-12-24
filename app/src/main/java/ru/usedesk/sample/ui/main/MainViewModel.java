package ru.usedesk.sample.ui.main;

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
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository;


public class MainViewModel extends ViewModel {

    private final Scheduler mainThreadScheduler;
    private final Scheduler workScheduler;

    private final ConfigurationRepository configurationRepository;

    private final MutableLiveData<Navigation> navigationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Configuration> configurationLiveData = new MutableLiveData<>();

    private final CompositeDisposable disposables = new CompositeDisposable();

    private Configuration configuration;

    public MainViewModel() {
        mainThreadScheduler = DI.getInstance().getMainThreadScheduler();
        workScheduler = DI.getInstance().getWorkScheduler();

        configurationRepository = DI.getInstance().getConfigurationRepository();

        navigationLiveData.setValue(Navigation.CONFIGURATION);
    }

    void goBack() {
        Navigation currentNavigation = navigationLiveData.getValue();
        if (currentNavigation == Navigation.CONFIGURATION) {
            navigationLiveData.setValue(Navigation.EXIT);
        } else if (currentNavigation == Navigation.SDK_CHAT && configuration.isWithKnowledgeBase()) {
            navigationLiveData.setValue(Navigation.SDK_KNOWLEDGE_BASE);
        } else {
            navigationLiveData.setValue(Navigation.CONFIGURATION);
        }
    }

    void goInfo() {
        navigationLiveData.setValue(Navigation.INFO);
    }

    void goSdk() {
        disposables.add(Single.create((SingleOnSubscribe<Configuration>) emitter -> emitter.onSuccess(configurationRepository.getConfiguration()))
                .subscribeOn(workScheduler)
                .observeOn(mainThreadScheduler)
                .subscribe(configuration -> {
                    this.configuration = configuration;
                    configurationLiveData.setValue(configuration);
                    if (this.configuration.isWithKnowledgeBase()) {
                        navigationLiveData.setValue(Navigation.SDK_KNOWLEDGE_BASE);
                    } else {
                        navigationLiveData.setValue(Navigation.SDK_CHAT);
                    }
                }));
    }

    @NonNull
    LiveData<Navigation> getNavigationLiveData() {
        return navigationLiveData;
    }

    @NonNull
    LiveData<Configuration> getConfigurationLiveData() {
        return configurationLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposables.dispose();
    }

    public enum Navigation {
        CONFIGURATION,
        SDK_KNOWLEDGE_BASE,
        SDK_CHAT,
        INFO,
        EXIT
    }
}
