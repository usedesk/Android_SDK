package ru.usedesk.chat_sdk.external.entity;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.usedesk.common_sdk.external.entity.exceptions.Validators;

public class UsedeskChatConfiguration {
    private static final String COMPANY_ID_KEY = "companyIdKey";
    private static final String EMAIL_KEY = "emailKey";
    private static final String SOCKET_URL_KEY = "urlKey";
    private static final String SECURE_URL_KEY = "offlineFormUrlKey";
    private static final String NAME_KEY = "nameKey";
    private static final String PHONE_KEY = "phoneKey";
    private static final String ADDITIONAL_ID_KEY = "additionalIdKey";
    private static final String INIT_CLIENT_MESSAGE_KEY = "initClientMessageKey";

    private final String companyId;
    private final String email;
    private final String socketUrl;
    private final String secureUrl;

    private final String clientName;
    private final Long clientPhoneNumber;
    private final Long clientAdditionalId;

    private final String initClientMessage;

    public UsedeskChatConfiguration(@NonNull String companyId, @NonNull String email,
                                    @NonNull String socketUrl, @NonNull String secureUrl) {
        this(companyId, email, socketUrl, secureUrl, null, null, null, null);
    }

    public UsedeskChatConfiguration(@NonNull String companyId, @NonNull String email,
                                    @NonNull String socketUrl, @NonNull String secureUrl,
                                    @Nullable String clientName, @Nullable Long clientPhoneNumber,
                                    @Nullable Long clientAdditionalId, @Nullable String initClientMessage) {
        this.companyId = companyId;
        this.email = email;
        this.socketUrl = socketUrl;
        this.secureUrl = secureUrl;
        this.clientName = clientName;
        this.clientPhoneNumber = clientPhoneNumber;
        this.clientAdditionalId = clientAdditionalId;
        this.initClientMessage = initClientMessage;
    }

    @NonNull
    public static UsedeskChatConfiguration deserialize(@NonNull Intent intent) {
        Long additionalId = null;
        Long phone = null;
        if (intent.hasExtra(PHONE_KEY)) {
            phone = intent.getExtras().getLong(PHONE_KEY);
        }
        if (intent.hasExtra(ADDITIONAL_ID_KEY)) {
            additionalId = intent.getExtras().getLong(ADDITIONAL_ID_KEY);
        }
        return new UsedeskChatConfiguration(intent.getStringExtra(COMPANY_ID_KEY),
                intent.getStringExtra(EMAIL_KEY),
                intent.getStringExtra(SOCKET_URL_KEY),
                intent.getStringExtra(SECURE_URL_KEY),
                intent.getStringExtra(NAME_KEY),
                phone,
                additionalId,
                intent.getStringExtra(INIT_CLIENT_MESSAGE_KEY));
    }

    private static boolean equals(@Nullable Object obj1, @Nullable Object obj2) {
        if (obj1 != null && obj2 != null) {
            return obj1.equals(obj2);
        }
        return obj1 == obj2;
    }

    public void serialize(@NonNull Intent intent) {
        intent.putExtra(COMPANY_ID_KEY, companyId);
        intent.putExtra(EMAIL_KEY, email);
        intent.putExtra(SOCKET_URL_KEY, socketUrl);
        intent.putExtra(SECURE_URL_KEY, secureUrl);
        intent.putExtra(NAME_KEY, clientName);
        if (clientPhoneNumber != null) {
            intent.putExtra(PHONE_KEY, clientPhoneNumber);
        }
        if (clientAdditionalId != null) {
            intent.putExtra(ADDITIONAL_ID_KEY, clientAdditionalId);
        }
        intent.putExtra(INIT_CLIENT_MESSAGE_KEY, initClientMessage);
    }

    public boolean isValid() {
        String phoneNumber = clientPhoneNumber != null
                ? clientPhoneNumber.toString()
                : null;
        return Validators.isValidPhonePhone(phoneNumber)
                && Validators.isValidEmailNecessary(email);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UsedeskChatConfiguration) {
            UsedeskChatConfiguration configuration = (UsedeskChatConfiguration) obj;
            return equals(this.companyId, configuration.companyId) &&
                    equals(this.email, configuration.email) &&
                    equals(this.socketUrl, configuration.socketUrl) &&
                    equals(this.secureUrl, configuration.secureUrl) &&
                    equals(this.clientName, configuration.clientName) &&
                    equals(this.clientPhoneNumber, configuration.clientPhoneNumber) &&
                    equals(this.clientAdditionalId, configuration.clientAdditionalId) &&
                    equals(this.initClientMessage, configuration.initClientMessage);
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
    public String getSocketUrl() {
        return socketUrl;
    }

    @NonNull
    public String getSecureUrl() {
        return secureUrl;
    }

    @Nullable
    public String getClientName() {
        return clientName;
    }

    @Nullable
    public Long getClientPhoneNumber() {
        return clientPhoneNumber;
    }

    @Nullable
    public Long getClientAdditionalId() {
        return clientAdditionalId;
    }

    @Nullable
    public String getInitClientMessage() {
        return initClientMessage;
    }
}