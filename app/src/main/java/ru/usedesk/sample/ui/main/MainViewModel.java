package ru.usedesk.sample.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.disposables.CompositeDisposable;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile;
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskKnowledgeBaseConfiguration;
import ru.usedesk.sample.ServiceLocator;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository;
import ru.usedesk.sample.ui._common.Event;
import ru.usedesk.sample.ui._common.OneTimeEvent;


public class MainViewModel extends ViewModel {

    private final ConfigurationRepository configurationRepository;

    private final MutableLiveData<Configuration> configurationLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> errorLiveData = new MutableLiveData<>();

    private final CompositeDisposable disposables = new CompositeDisposable();
    private MainNavigation mainNavigation;

    private Configuration configuration;

    public MainViewModel() {
        configurationRepository = ServiceLocator.getInstance().getConfigurationRepository();
    }

    void init(MainNavigation mainNavigation) {
        this.mainNavigation = mainNavigation;
        mainNavigation.goConfiguration();
    }

    void goInfo() {
        mainNavigation.goInfo();
    }

    void goShowFile(@NonNull UsedeskFile usedeskFile) {
        mainNavigation.goShowFile(usedeskFile);
    }

    void goShowHtml(@NonNull String htmlText) {
        mainNavigation.goShowHtml(htmlText);
    }

    void goSdk(@Nullable String customAgentName) {
        disposables.add(configurationRepository.getConfiguration().subscribe(configuration -> {
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

                configurationLiveData.postValue(configuration);
                if (this.configuration.isWithKnowledgeBase()) {
                    mainNavigation.goKnowledgeBase();
                } else {
                    mainNavigation.goChat(customAgentName);
                }
            } else {
                errorLiveData.postValue(new OneTimeEvent<>("Invalid configuration"));
            }
        }));
    }

    public void goChat(@Nullable String customAgentName) {
        mainNavigation.goChat(customAgentName);
    }

    void onBackPressed() {
        mainNavigation.onBackPressed();
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
}
