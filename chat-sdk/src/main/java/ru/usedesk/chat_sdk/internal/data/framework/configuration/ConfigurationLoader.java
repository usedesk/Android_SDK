package ru.usedesk.chat_sdk.internal.data.framework.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.internal.data.framework.info.DataLoader;

@Singleton
public class ConfigurationLoader extends DataLoader<UsedeskChatConfiguration> {
    private static final String PREF_NAME = "usedeskSdkConfiguration";
    private static final String KEY_ID = "id";
    private static final String KEY_URL = "url";
    private static final String KEY_OFFLINE_URL = "offlineUrl";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDITIONAL_ID = "additionalId";

    private final SharedPreferences sharedPreferences;

    @Inject
    @Named("configuration")
    ConfigurationLoader(@NonNull Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    private Long getLong(@Nullable String value) {
        return value == null || value.isEmpty()
                ? null
                : Long.valueOf(value);
    }

    @Nullable
    private String getString(@Nullable Long value) {
        return value == null
                ? null
                : value.toString();
    }

    @Override
    @Nullable
    protected UsedeskChatConfiguration loadData() {
        final String id = sharedPreferences.getString(KEY_ID, null);
        final String url = sharedPreferences.getString(KEY_URL, null);
        final String offlineUrl = sharedPreferences.getString(KEY_OFFLINE_URL, null);
        final String email = sharedPreferences.getString(KEY_EMAIL, null);

        if (id == null || url == null || email == null || offlineUrl == null) {
            return null;
        }

        String name = null;
        String phone = null;
        String additionalId = null;

        try {
            name = sharedPreferences.getString(KEY_NAME, null);
            phone = sharedPreferences.getString(KEY_PHONE, null);
            additionalId = sharedPreferences.getString(KEY_ADDITIONAL_ID, null);
        } catch (ClassCastException e) {
            try {
                phone = String.valueOf(sharedPreferences.getLong(KEY_PHONE, 0));//Для миграции с версий, где хранился Long
                additionalId = String.valueOf(sharedPreferences.getLong(KEY_ADDITIONAL_ID, 0));
            } catch (ClassCastException e1) {
                e.printStackTrace();
            }
        }

        return new UsedeskChatConfiguration(id, email, url, offlineUrl, name, getLong(phone), getLong(additionalId));
    }

    @Override
    protected void saveData(@NonNull UsedeskChatConfiguration configuration) {
        sharedPreferences.edit()
                .putString(KEY_ID, configuration.getCompanyId())
                .putString(KEY_URL, configuration.getUrl())
                .putString(KEY_OFFLINE_URL, configuration.getOfflineFormUrl())
                .putString(KEY_EMAIL, configuration.getEmail())
                .putString(KEY_NAME, configuration.getClientName())
                .putString(KEY_ADDITIONAL_ID, getString(configuration.getClientAdditionalId()))
                .putString(KEY_PHONE, getString(configuration.getClientPhoneNumber()))
                .apply();
    }

    @Override
    public void clearData() {
        super.clearData();

        sharedPreferences.edit()
                .remove(KEY_ID)
                .remove(KEY_URL)
                .remove(KEY_OFFLINE_URL)
                .remove(KEY_EMAIL)
                .apply();
    }
}
