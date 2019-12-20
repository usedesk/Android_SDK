package ru.usedesk.sample.model.configuration.entity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ConfigurationModel {

    private String companyId;
    private String email;
    private String url;
    private String offlineFormUrl;
    private String accountId;
    private String token;
    private String clientName;
    private String clientPhoneNumber;
    private String clientAdditionalId;
    private Boolean foregroundService;
    private Boolean customViews;
    private Boolean withKnowledgeBase;

    private ConfigurationModel(@Nullable ConfigurationModel model) {
        if (model != null) {
            this.companyId = model.companyId;
            this.email = model.email;
            this.url = model.url;
            this.offlineFormUrl = model.offlineFormUrl;
            this.accountId = model.accountId;
            this.token = model.token;
            this.clientName = model.clientName;
            this.clientPhoneNumber = model.clientPhoneNumber;
            this.clientAdditionalId = model.clientAdditionalId;
            this.foregroundService = model.foregroundService;
            this.customViews = model.customViews;
            this.withKnowledgeBase = model.withKnowledgeBase;
        }
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

    public static class Builder {
        private ConfigurationModel configurationModel;

        public Builder(@Nullable ConfigurationModel configurationModel) {
            this.configurationModel = new ConfigurationModel(configurationModel);
        }

        @NonNull
        public Builder setCompanyId(@NonNull String companyId) {
            configurationModel.companyId = companyId;
            return this;
        }

        @NonNull
        public Builder setEmail(@NonNull String email) {
            configurationModel.email = email;
            return this;
        }

        @NonNull
        public Builder setUrl(@NonNull String url) {
            configurationModel.url = url;
            return this;
        }

        @NonNull
        public Builder setOfflineFormUrl(@NonNull String offlineFormUrl) {
            configurationModel.offlineFormUrl = offlineFormUrl;
            return this;
        }

        @NonNull
        public Builder setAccountId(@NonNull String accountId) {
            configurationModel.accountId = accountId;
            return this;
        }

        @NonNull
        public Builder setToken(@NonNull String token) {
            configurationModel.token = token;
            return this;
        }

        @NonNull
        public Builder setClientName(@NonNull String clientName) {
            configurationModel.clientName = clientName;
            return this;
        }

        @NonNull
        public Builder setClientPhoneNumber(@NonNull String clientPhoneNumber) {
            configurationModel.clientPhoneNumber = clientPhoneNumber;
            return this;
        }

        @NonNull
        public Builder setClientAdditionalId(@NonNull String clientAdditionalId) {
            configurationModel.clientAdditionalId = clientAdditionalId;
            return this;
        }

        @NonNull
        public Builder setForegroundService(@NonNull Boolean foregroundService) {
            configurationModel.foregroundService = foregroundService;
            return this;
        }

        @NonNull
        public Builder setCustomViews(@NonNull Boolean customViews) {
            configurationModel.customViews = customViews;
            return this;
        }

        @NonNull
        public Builder setWithKnowledgeBase(@NonNull Boolean withKnowledgeBase) {
            configurationModel.withKnowledgeBase = withKnowledgeBase;
            return this;
        }

        @NonNull
        public ConfigurationModel build() {
            return new ConfigurationModel(configurationModel);
        }
    }
}
