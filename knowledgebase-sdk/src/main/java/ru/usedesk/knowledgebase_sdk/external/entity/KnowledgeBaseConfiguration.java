package ru.usedesk.knowledgebase_sdk.external.entity;

import androidx.annotation.NonNull;

public class KnowledgeBaseConfiguration {
    private final String accountId;
    private final String token;

    public KnowledgeBaseConfiguration(@NonNull String accountId, @NonNull String token) {
        this.accountId = accountId;
        this.token = token;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getToken() {
        return token;
    }
}
