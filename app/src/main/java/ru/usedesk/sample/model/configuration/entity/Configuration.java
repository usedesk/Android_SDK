package ru.usedesk.sample.model.configuration.entity;

import androidx.annotation.NonNull;

public class Configuration {

    private final String companyId;
    private final String email;
    private final String url;
    private final String offlineFormUrl;
    private final String accountId;
    private final String token;
    private final String clientName;
    private final String clientPhoneNumber;
    private final String clientAdditionalId;
    private final String initClientMessage;
    private final String customAgentName;
    private final Boolean foregroundService;
    private final Boolean withKnowledgeBase;

    public Configuration(@NonNull String companyId,
                         @NonNull String email,
                         @NonNull String url,
                         @NonNull String offlineFormUrl,
                         @NonNull String accountId,
                         @NonNull String token,
                         @NonNull String clientName,
                         @NonNull String clientPhoneNumber,
                         @NonNull String clientAdditionalId,
                         @NonNull String initClientMessage,
                         @NonNull String customAgentName,
                         @NonNull Boolean foregroundService,
                         @NonNull Boolean withKnowledgeBase) {
        this.companyId = companyId;
        this.email = email;
        this.url = url;
        this.offlineFormUrl = offlineFormUrl;
        this.accountId = accountId;
        this.token = token;
        this.clientName = clientName;
        this.clientPhoneNumber = clientPhoneNumber;
        this.clientAdditionalId = clientAdditionalId;
        this.foregroundService = foregroundService;
        this.withKnowledgeBase = withKnowledgeBase;
        this.initClientMessage = initClientMessage;
        this.customAgentName = customAgentName;
    }

    @NonNull
    public String getCompanyId() {
        return companyId;
    }

    @NonNull
    public String getClientEmail() {
        return email;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    @NonNull
    public String getOfflineFormUrl() {
        return offlineFormUrl;
    }

    @NonNull
    public String getAccountId() {
        return accountId;
    }

    @NonNull
    public String getToken() {
        return token;
    }

    @NonNull
    public String getClientName() {
        return clientName;
    }

    @NonNull
    public String getClientPhoneNumber() {
        return clientPhoneNumber;
    }

    @NonNull
    public String getClientAdditionalId() {
        return clientAdditionalId;
    }

    @NonNull
    public String getInitClientMessage() {
        return initClientMessage;
    }

    @NonNull
    public String getCustomAgentName() {
        return customAgentName;
    }

    @NonNull
    public Boolean isForegroundService() {
        return foregroundService;
    }

    @NonNull
    public Boolean isWithKnowledgeBase() {
        return withKnowledgeBase;
    }
}
