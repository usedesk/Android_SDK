package ru.usedesk.sample.model.configuration.entity;

import androidx.annotation.NonNull;

public class Configuration {

    private final String urlChat;
    private final String urlOfflineForm;
    private final String urlToSendFile;
    private final String urlApi;
    private final String companyId;
    private final String accountId;
    private final String token;
    private final String clientEmail;
    private final String clientName;
    private final String clientPhoneNumber;
    private final String clientAdditionalId;
    private final String clientInitMessage;
    private final String customAgentName;
    private final Boolean foregroundService;
    private final Boolean withKnowledgeBase;

    public Configuration(
            @NonNull String urlChat,
            @NonNull String urlOfflineForm,
            @NonNull String urlToSendFile,
            @NonNull String urlApi,
            @NonNull String companyId,
            @NonNull String accountId,
            @NonNull String token,
            @NonNull String clientEmail,
            @NonNull String clientName,
            @NonNull String clientPhoneNumber,
            @NonNull String clientAdditionalId,
            @NonNull String clientInitMessage,
            @NonNull String customAgentName,
            @NonNull Boolean foregroundService,
            @NonNull Boolean withKnowledgeBase) {
        this.urlChat = urlChat;
        this.urlOfflineForm = urlOfflineForm;
        this.urlToSendFile = urlToSendFile;
        this.urlApi = urlApi;
        this.companyId = companyId;
        this.accountId = accountId;
        this.token = token;
        this.clientEmail = clientEmail;
        this.clientName = clientName;
        this.clientPhoneNumber = clientPhoneNumber;
        this.clientAdditionalId = clientAdditionalId;
        this.clientInitMessage = clientInitMessage;
        this.customAgentName = customAgentName;
        this.foregroundService = foregroundService;
        this.withKnowledgeBase = withKnowledgeBase;
    }

    @NonNull
    public String getUrlChat() {
        return urlChat;
    }

    @NonNull
    public String getUrlOfflineForm() {
        return urlOfflineForm;
    }

    @NonNull
    public String getUrlToSendFile() {
        return urlToSendFile;
    }

    @NonNull
    public String getUrlApi() {
        return urlApi;
    }

    @NonNull
    public String getCompanyId() {
        return companyId;
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
    public String getClientEmail() {
        return clientEmail;
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
    public String getClientInitMessage() {
        return clientInitMessage;
    }

    @NonNull
    public String getCustomAgentName() {
        return customAgentName;
    }

    public boolean isForegroundService() {
        return foregroundService;
    }

    public boolean isWithKnowledgeBase() {
        return withKnowledgeBase;
    }
}
