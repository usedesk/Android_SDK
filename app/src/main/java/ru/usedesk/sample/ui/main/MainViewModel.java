package ru.usedesk.sample.ui.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.disposables.CompositeDisposable;
import ru.usedesk.chat_sdk.UsedeskChatSdk;
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.entity.UsedeskFile;
import ru.usedesk.common_sdk.entity.UsedeskEvent;
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent;
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration;
import ru.usedesk.sample.ServiceLocator;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository;


public class MainViewModel extends ViewModel {

    private final ConfigurationRepository configurationRepository;

    private final MutableLiveData<Configuration> configurationLiveData = new MutableLiveData<>();
    private final MutableLiveData<UsedeskEvent<String>> errorLiveData = new MutableLiveData<>();

    private final CompositeDisposable disposables = new CompositeDisposable();
    private MainNavigation mainNavigation;

    private Configuration configuration;
    private Boolean inited = false;

    public MainViewModel() {
        configurationRepository = ServiceLocator.getInstance().getConfigurationRepository();
    }

    void init(MainNavigation mainNavigation) {
        this.mainNavigation = mainNavigation;
        if (!inited) {
            inited = true;
            mainNavigation.goConfiguration();
        }
    }

    void goShowFile(@NonNull UsedeskFile usedeskFile) {
        mainNavigation.goShowFile(usedeskFile);
    }

    void goSdk() {
        disposables.add(configurationRepository.getConfiguration().subscribe(configuration -> {
            UsedeskChatConfiguration defaultChatConfiguration = new UsedeskChatConfiguration(
                    configuration.getUrlChat(),
                    configuration.getUrlOfflineForm(),
                    configuration.getClientEmail()
            );
            String urlToSendFile = configuration.getUrlToSendFile();
            if (urlToSendFile.isEmpty()) {
                urlToSendFile = defaultChatConfiguration.getUrlToSendFile();
            }
            String urlOfflineForm = configuration.getUrlOfflineForm();
            if (urlOfflineForm.isEmpty()) {
                urlOfflineForm = defaultChatConfiguration.getUrlOfflineForm();
            }
            UsedeskChatConfiguration usedeskChatConfiguration = new UsedeskChatConfiguration(
                    configuration.getUrlChat(),
                    urlOfflineForm,
                    urlToSendFile,
                    configuration.getCompanyId(),
                    configuration.getChannelId(),
                    configuration.getClientSignature(),
                    configuration.getClientEmail(),
                    configuration.getClientName(),
                    configuration.getClientNote(),
                    configuration.getClientPhoneNumber(),
                    configuration.getClientAdditionalId(),
                    configuration.getClientInitMessage());

            if (usedeskChatConfiguration.validate().isAllValid()) {
                this.configuration = configuration;
                initUsedeskConfiguration(usedeskChatConfiguration, configuration.getWithKb());

                configurationLiveData.postValue(configuration);
                if (this.configuration.getWithKb()) {
                    mainNavigation.goKnowledgeBase(configuration.getWithKbSupportButton(),
                            configuration.getWithKbArticleRating());
                } else {
                    mainNavigation.goChat(configuration.getCustomAgentName());
                }
            } else {
                errorLiveData.postValue(new UsedeskSingleLifeEvent<>("Invalid configuration"));
            }
        }));
    }

    public void goChat() {
        disposables.add(configurationRepository.getConfiguration().subscribe(configuration -> {
            mainNavigation.goChat(configuration.getCustomAgentName());
        }));
    }

    void onBackPressed() {
        mainNavigation.onBackPressed();
    }

    private void initUsedeskConfiguration(@NonNull UsedeskChatConfiguration usedeskChatConfiguration,
                                          boolean withKnowledgeBase) {
        UsedeskChatSdk.setConfiguration(usedeskChatConfiguration);

        if (withKnowledgeBase) {
            UsedeskKnowledgeBaseConfiguration defaultConfiguration = new UsedeskKnowledgeBaseConfiguration(
                    configuration.getAccountId(),
                    configuration.getToken(),
                    configuration.getClientEmail()
            );
            String urlApi = configuration.getUrlApi();
            if (urlApi.isEmpty()) {
                urlApi = defaultConfiguration.getUrlApi();
            }
            UsedeskKnowledgeBaseSdk.setConfiguration(new UsedeskKnowledgeBaseConfiguration(
                    urlApi,
                    configuration.getAccountId(),
                    configuration.getToken(),
                    configuration.getClientEmail(),
                    configuration.getClientName()));
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
    LiveData<UsedeskEvent<String>> getErrorLiveData() {
        return errorLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposables.dispose();
    }
}
