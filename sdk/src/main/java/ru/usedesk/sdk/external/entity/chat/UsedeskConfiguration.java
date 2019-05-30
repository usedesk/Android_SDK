package ru.usedesk.sdk.external.entity.chat;

import android.content.Intent;
import android.support.annotation.NonNull;

public class UsedeskConfiguration {

    private final String companyId;
    private final String email;
    private final String url;

    public UsedeskConfiguration(@NonNull String companyId, @NonNull String email, @NonNull String url) {
        this.companyId = companyId;
        this.email = email;
        this.url = url;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getEmail() {
        return email;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UsedeskConfiguration) {
            UsedeskConfiguration configuration = (UsedeskConfiguration) obj;
            return this.companyId.equals(configuration.companyId) &&
                    this.email.equals(configuration.email) &&
                    this.url.equals(configuration.url);
        }
        return false;
    }

    @NonNull
    public static UsedeskConfiguration deserialize(@NonNull Intent intent) {
        return new UsedeskConfiguration(intent.getStringExtra("companyIdKey"),
                intent.getStringExtra("emailKey"),
                intent.getStringExtra("urlKey"));
    }

    public void serialize(@NonNull Intent intent) {
        intent.putExtra("companyIdKey", companyId);
        intent.putExtra("emailKey", email);
        intent.putExtra("urlKey", url);
    }
}