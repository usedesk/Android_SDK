package ru.usedesk.sample.model.configuration.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Configuration {

    private String companyId;
    private String email;
    private String url;
    private String offlineFormUrl;
    private String accountId;
    private String token;
    private String clientName;
    private String clientPhoneNumber;
    private String clientAdditionalId;
    private String initClientMessage;
    private Boolean foregroundService;
    private Boolean customViews;
    private Boolean withKnowledgeBase;

    public Configuration(@NonNull String companyId,
                         @NonNull String email,
                         @NonNull String url,
                         @NonNull String offlineFormUrl,
                         @NonNull String accountId,
                         @NonNull String token,
                         @Nullable String clientName,
                         @Nullable String clientPhoneNumber,
                         @Nullable String clientAdditionalId,
                         @Nullable String initClientMessage,
                         @Nullable Boolean foregroundService,
                         @Nullable Boolean customViews,
                         @Nullable Boolean withKnowledgeBase) {
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
        this.customViews = customViews;
        this.withKnowledgeBase = withKnowledgeBase;
        this.initClientMessage = initClientMessage;
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
