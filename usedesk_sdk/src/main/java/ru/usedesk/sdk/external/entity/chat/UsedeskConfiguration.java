package ru.usedesk.sdk.external.entity.chat;

import android.content.Intent;
import android.support.annotation.NonNull;

public class UsedeskConfiguration {
    private static final String COMPANY_ID_KEY = "companyIdKey";
    private static final String EMAIL_KEY = "emailKey";
    private static final String URL_KEY = "urlKey";
    private static final String OFFLINE_FORM_URL_KEY = "offlineFormUrlKey";

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
        return new UsedeskConfiguration(intent.getStringExtra(COMPANY_ID_KEY),
                intent.getStringExtra(EMAIL_KEY),
                intent.getStringExtra(URL_KEY),
                intent.getStringExtra(OFFLINE_FORM_URL_KEY));
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
        intent.putExtra(COMPANY_ID_KEY, companyId);
        intent.putExtra(EMAIL_KEY, email);
        intent.putExtra(URL_KEY, url);
        intent.putExtra(OFFLINE_FORM_URL_KEY, offlineFormUrl);
    }
}