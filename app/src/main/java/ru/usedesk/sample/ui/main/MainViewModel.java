package ru.usedesk.sample.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.disposables.CompositeDisposable;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskKnowledgeBaseConfiguration;
import ru.usedesk.sample.ServiceLocator;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository;
import ru.usedesk.sample.ui._common.Event;
import ru.usedesk.sample.ui._common.OneTimeEvent;


public class MainViewModel extends ViewModel {

    private final ConfigurationRepository configurationRepository;

    private final MutableLiveData<Event<Navigation>> navigationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Configuration> configurationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> errorLiveData = new MutableLiveData<>();

    private final CompositeDisposable disposables = new CompositeDisposable();

    private Configuration configuration;

    public MainViewModel() {
        configurationRepository = ServiceLocator.getInstance().getConfigurationRepository();

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
        disposables.add(configurationRepository.getConfiguration()
                .subscribe(configuration -> {
                    UsedeskChatConfiguration usedeskChatConfiguration = new UsedeskChatConfiguration(configuration.getCompanyId(),
                            configuration.getEmail(),
                            configuration.getUrl(),
                            configuration.getOfflineFormUrl(),
                            configuration.getClientName(),
                            getLong(configuration.getClientPhoneNumber()),
                            getLong(configuration.getClientAdditionalId()),
                            configuration.getInitClientMessage());
                    if (usedeskChatConfiguration.isValid()) {
                        this.configuration = configuration;
                        initUsedeskConfiguration(usedeskChatConfiguration, configuration.isWithKnowledgeBase());

                        configurationLiveData.setValue(configuration);
                        if (this.configuration.isWithKnowledgeBase()) {
                            setNavigation(Navigation.SDK_KNOWLEDGE_BASE);
                        } else {
                            setNavigation(Navigation.SDK_CHAT);
                        }
                    } else {
                        errorLiveData.postValue(new OneTimeEvent<>("Invalid configuration"));
                    }
                }));
    }

    private void initUsedeskConfiguration(@NonNull UsedeskChatConfiguration usedeskChatConfiguration,
                                          boolean withKnowledgeBase) {
        UsedeskChatSdk.setConfiguration(usedeskChatConfiguration);

        if (withKnowledgeBase) {
            UsedeskKnowledgeBaseSdk.setConfiguration(new UsedeskKnowledgeBaseConfiguration(configuration.getAccountId(), configuration.getToken()));
        }
    }

    private Long getLong(@Nullable String value) {
        return value == null || value.isEmpty()
                ? null
                : Long.valueOf(value);
    }

    @NonNull
    LiveData<Event<Navigation>> getNavigationLiveData() {
        return navigationLiveData;
    }

    @NonNull
    LiveData<Configuration> getConfigurationLiveData() {
        return configurationLiveData;
    }

    @NonNull
    LiveData<Event<String>> getErrorLiveData() {
        return errorLiveData;
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
