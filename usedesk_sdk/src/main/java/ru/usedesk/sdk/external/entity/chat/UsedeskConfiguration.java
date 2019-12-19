package ru.usedesk.sdk.external.entity.chat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class UsedeskConfiguration {
    private static final String COMPANY_ID_KEY = "companyIdKey";
    private static final String EMAIL_KEY = "emailKey";
    private static final String URL_KEY = "urlKey";
    private static final String OFFLINE_FORM_URL_KEY = "offlineFormUrlKey";
    private static final String NAME_KEY = "nameKey";
    private static final String PHONE_KEY = "phoneKey";
    private static final String ADDITIONAL_ID_KEY = "additionalIdKey";

    private final String companyId;
    private final String email;
    private final String url;
    private final String offlineFormUrl;
    private final String name;
    private final Long phone;
    private final Long additionalId;

    public UsedeskConfiguration(@NonNull String companyId, @NonNull String email,
                                @NonNull String url, @NonNull String offlineFormUrl) {
        this(companyId, email, url, offlineFormUrl, null, null, null);
    }

    public UsedeskConfiguration(@NonNull String companyId, @NonNull String email,
                                @NonNull String url, @NonNull String offlineFormUrl,
                                @Nullable String name, @Nullable Long phone, @Nullable Long additionalId) {
        this.companyId = companyId;
        this.email = email;
        this.url = url;
        this.offlineFormUrl = offlineFormUrl;
        this.name = name;
        this.phone = phone;
        this.additionalId = additionalId;
    }

    @NonNull
    public static UsedeskConfiguration deserialize(@NonNull Intent intent) {
        Long additionalId = null;
        Long phone = null;
        if (intent.getExtras() != null) {
            phone = intent.getExtras().getLong(PHONE_KEY);
            additionalId = intent.getExtras().getLong(ADDITIONAL_ID_KEY);
        }
        return new UsedeskConfiguration(intent.getStringExtra(COMPANY_ID_KEY),
                intent.getStringExtra(EMAIL_KEY),
                intent.getStringExtra(URL_KEY),
                intent.getStringExtra(OFFLINE_FORM_URL_KEY),
                intent.getStringExtra(NAME_KEY),
                phone,
                additionalId);
    }

    public void serialize(@NonNull Intent intent) {
        intent.putExtra(COMPANY_ID_KEY, companyId);
        intent.putExtra(EMAIL_KEY, email);
        intent.putExtra(URL_KEY, url);
        intent.putExtra(OFFLINE_FORM_URL_KEY, offlineFormUrl);
        intent.putExtra(NAME_KEY, name);
        intent.putExtra(PHONE_KEY, phone);
        intent.putExtra(ADDITIONAL_ID_KEY, additionalId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UsedeskConfiguration) {
            UsedeskConfiguration configuration = (UsedeskConfiguration) obj;
            return equals(this.companyId, configuration.companyId) &&
                    equals(this.email, configuration.email) &&
                    equals(this.url, configuration.url) &&
                    equals(this.offlineFormUrl, configuration.offlineFormUrl) &&
                    equals(this.name, configuration.name) &&
                    equals(this.phone, configuration.phone) &&
                    equals(this.additionalId, configuration.additionalId);
        }
        return false;
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

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public Long getPhone() {
        return phone;
    }

    @Nullable
    public Long getAdditionalId() {
        return additionalId;
    }

    private boolean equals(@Nullable Object obj1, @Nullable Object obj2) {
        if (obj1 != null && obj2 != null) {
            return obj1.equals(obj2);
        }
        return obj1 == obj2;
    }
}