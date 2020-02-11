package ru.usedesk.knowledgebase_sdk.external.entity;

import androidx.annotation.NonNull;

public class UsedeskKnowledgeBaseConfiguration {
    private final String accountId;
    private final String token;

    public UsedeskKnowledgeBaseConfiguration(@NonNull String accountId, @NonNull String token) {
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
