package ru.usedesk.sample.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import ru.usedesk.sample.DI;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository;
import ru.usedesk.sample.ui._common.Event;
import ru.usedesk.sample.ui._common.OneTimeEvent;


public class MainViewModel extends ViewModel {

    private final Scheduler workScheduler;

    private final ConfigurationRepository configurationRepository;

    private final MutableLiveData<Event<Navigation>> navigationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Configuration> configurationLiveData = new MutableLiveData<>();

    private final CompositeDisposable disposables = new CompositeDisposable();

    private Configuration configuration;

    public MainViewModel() {
        workScheduler = DI.getInstance().getWorkScheduler();

        configurationRepository = DI.getInstance().getConfigurationRepository();

        setNavigation(Navigation.CONFIGURATION);
    }

    void goBack() {
        Navigation currentNavigation = navigationLiveData.getValue().getData();
        if (currentNavigation == Navigation.CONFIGURATION) {
            setNavigation(Navigation.EXIT);
        } else if (currentNavigation == Navigation.SDK_CHAT && configuration.isWithKnowledgeBase()) {
            setNavigation(Navigation.SDK_KNOWLEDGE_BASE);
        } else {
            setNavigation(Navigation.CONFIGURATION);
        }
    }

    private void setNavigation(@NonNull Navigation navigation) {
        navigationLiveData.setValue(new OneTimeEvent<>(navigation));
    }

    void goInfo() {
        setNavigation(Navigation.INFO);
    }

    void goSdk() {
        disposables.add(Single.create((SingleOnSubscribe<Configuration>) emitter -> emitter.onSuccess(configurationRepository.getConfiguration()))
                .subscribeOn(workScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(configuration -> {
                    this.configuration = configuration;
                    configurationLiveData.postValue(configuration);
                    if (this.configuration.isWithKnowledgeBase()) {
                        setNavigation(Navigation.SDK_KNOWLEDGE_BASE);
                    } else {
                        setNavigation(Navigation.SDK_CHAT);
                    }
                }));
    }

    @NonNull
    LiveData<Event<Navigation>> getNavigationLiveData() {
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
