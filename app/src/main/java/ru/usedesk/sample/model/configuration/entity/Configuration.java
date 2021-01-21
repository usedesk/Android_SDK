package ru.usedesk.sample.model.configuration.entity;

import androidx.annotation.NonNull;

public class Configuration {

    private final String companyId;
    private final String email;
    private final String socketUrl;
    private final String secureUrl;
    private final String accountId;
    private final String token;
    private final String clientName;
    private final String clientPhoneNumber;
    private final String clientAdditionalId;
    private final String initClientMessage;
    private final String customAgentName;
    private final Boolean foregroundService;
    private final Boolean customViews;
    private final Boolean withKnowledgeBase;

    public Configuration(@NonNull String companyId,
                         @NonNull String email,
                         @NonNull String socketUrl,
                         @NonNull String secureUrl,
                         @NonNull String accountId,
                         @NonNull String token,
                         @NonNull String clientName,
                         @NonNull String clientPhoneNumber,
                         @NonNull String clientAdditionalId,
                         @NonNull String initClientMessage,
                         @NonNull String customAgentName,
                         @NonNull Boolean foregroundService,
                         @NonNull Boolean customViews,
                         @NonNull Boolean withKnowledgeBase) {
        this.companyId = companyId;
        this.email = email;
        this.socketUrl = socketUrl;
        this.secureUrl = secureUrl;
        this.accountId = accountId;
        this.token = token;
        this.clientName = clientName;
        this.clientPhoneNumber = clientPhoneNumber;
        this.clientAdditionalId = clientAdditionalId;
        this.foregroundService = foregroundService;
        this.customViews = customViews;
        this.withKnowledgeBase = withKnowledgeBase;
        this.initClientMessage = initClientMessage;
        this.customAgentName = customAgentName;
    }

    @NonNull
    public String getCompanyId() {
        return companyId;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    @NonNull
    public String getSocketUrl() {
        return socketUrl;
    }

    @NonNull
    public String getSecureUrl() {
        return secureUrl;
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
    public Boolean isCustomViews() {
        return customViews;
    }

    @NonNull
    public Boolean isWithKnowledgeBase() {
        return withKnowledgeBase;
    }
}
