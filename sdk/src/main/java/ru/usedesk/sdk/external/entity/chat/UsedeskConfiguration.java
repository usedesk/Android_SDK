package ru.usedesk.sdk.external.entity.chat;

import android.content.Intent;
import android.support.annotation.NonNull;

public class UsedeskConfiguration {

    private final String companyId;
    private final String email;
    private final String url;
    private final String offlineFormUrl;

    public UsedeskConfiguration(@NonNull String companyId, @NonNull String email,
                                @NonNull String url, @NonNull String offlineFormUrl) {
        this.companyId = companyId;
        this.email = email;
        this.url = url;
        this.offlineFormUrl = offlineFormUrl;
    }

    @NonNull
    public static UsedeskConfiguration deserialize(@NonNull Intent intent) {
        return new UsedeskConfiguration(intent.getStringExtra("companyIdKey"),
                intent.getStringExtra("emailKey"),
                intent.getStringExtra("urlKey"),
                intent.getStringExtra("offlineFormUrlKey"));
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UsedeskConfiguration) {
            UsedeskConfiguration configuration = (UsedeskConfiguration) obj;
            return this.companyId.equals(configuration.companyId) &&
                    this.email.equals(configuration.email) &&
                    this.url.equals(configuration.url) &&
                    this.offlineFormUrl.equals(configuration.offlineFormUrl);
        }
        return false;
    }

    public void serialize(@NonNull Intent intent) {
        intent.putExtra("companyIdKey", companyId);
        intent.putExtra("emailKey", email);
        intent.putExtra("urlKey", url);
        intent.putExtra("offlineFormUrlKey", offlineFormUrl);
    }
}