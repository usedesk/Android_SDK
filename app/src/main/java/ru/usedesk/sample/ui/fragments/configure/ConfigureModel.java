package ru.usedesk.sample.ui.fragments.configure;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ConfigureModel {

    private Long companyId;
    private String email;
    private String url;
    private String offlineFormUrl;
    private Long accountId;
    private String token;
    private String clientName;
    private String clientPhoneNumber;
    private String clientAdditionalId;
    private Boolean foregroundService;
    private Boolean customViews;
    private Boolean withKnowledgeBase;

    private ConfigureModel(@Nullable ConfigureModel model) {
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

    @Nullable
    public Long getCompanyId() {
        return companyId;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public String getOfflineFormUrl() {
        return offlineFormUrl;
    }

    @Nullable
    public Long getAccountId() {
        return accountId;
    }

    @Nullable
    public String getToken() {
        return token;
    }

    @Nullable
    public String getClientName() {
        return clientName;
    }

    @Nullable
    public String getClientPhoneNumber() {
        return clientPhoneNumber;
    }

    @Nullable
    public String getClientAdditionalId() {
        return clientAdditionalId;
    }

    @Nullable
    public Boolean getForegroundService() {
        return foregroundService;
    }

    @Nullable
    public Boolean getCustomViews() {
        return customViews;
    }

    @Nullable
    public Boolean getWithKnowledgeBase() {
        return withKnowledgeBase;
    }

    public static class Builder {
        private ConfigureModel configureModel;

        public Builder(@Nullable ConfigureModel configureModel) {
            this.configureModel = new ConfigureModel(configureModel);
        }

        @NonNull
        public Builder setCompanyId(@Nullable Long companyId) {
            configureModel.companyId = companyId;
            return this;
        }

        @NonNull
        public Builder setEmail(@Nullable String email) {
            configureModel.email = email;
            return this;
        }

        @NonNull
        public Builder setUrl(@Nullable String url) {
            configureModel.url = url;
            return this;
        }

        @NonNull
        public Builder setOfflineFormUrl(@Nullable String offlineFormUrl) {
            configureModel.offlineFormUrl = offlineFormUrl;
            return this;
        }

        @NonNull
        public Builder setAccountId(@Nullable Long accountId) {
            configureModel.accountId = accountId;
            return this;
        }

        @NonNull
        public Builder setToken(@Nullable String token) {
            configureModel.token = token;
            return this;
        }

        @NonNull
        public Builder setClientName(@Nullable String clientName) {
            configureModel.clientName = clientName;
            return this;
        }

        @NonNull
        public Builder setClientPhoneNumber(@Nullable String clientPhoneNumber) {
            configureModel.clientPhoneNumber = clientPhoneNumber;
            return this;
        }

        @NonNull
        public Builder setClientAdditionalId(@Nullable String clientAdditionalId) {
            configureModel.clientAdditionalId = clientAdditionalId;
            return this;
        }

        @NonNull
        public Builder setForegroundService(@Nullable Boolean foregroundService) {
            configureModel.foregroundService = foregroundService;
            return this;
        }

        @NonNull
        public Builder setCustomViews(@Nullable Boolean customViews) {
            configureModel.customViews = customViews;
            return this;
        }

        @NonNull
        public Builder setWithKnowledgeBase(@Nullable Boolean withKnowledgeBase) {
            configureModel.withKnowledgeBase = withKnowledgeBase;
            return this;
        }

        @NonNull
        public ConfigureModel build() {
            return new ConfigureModel(configureModel);
        }
    }
}
