package ru.usedesk.sdk.internal.data.framework.loader;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.internal.data.repository.user.info.DataLoader;

@Singleton
public class ConfigurationLoader extends DataLoader<UsedeskConfiguration> {
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

    @Override
    @Nullable
    protected UsedeskConfiguration loadData() {
        final String id = sharedPreferences.getString(KEY_ID, null);
        final String url = sharedPreferences.getString(KEY_URL, null);
        final String offlineUrl = sharedPreferences.getString(KEY_OFFLINE_URL, null);
        final String email = sharedPreferences.getString(KEY_EMAIL, null);

        if (id == null || url == null || email == null || offlineUrl == null) {
            return null;
        }
        final String name = sharedPreferences.getString(KEY_NAME, null);
        final Long phone = sharedPreferences.contains(KEY_ADDITIONAL_ID)
                ? sharedPreferences.getLong(KEY_ADDITIONAL_ID, 0)
                : null;
        final Long additionalId = sharedPreferences.contains(KEY_ADDITIONAL_ID)
                ? sharedPreferences.getLong(KEY_ADDITIONAL_ID, 0)
                : null;

        return new UsedeskConfiguration(id, email, url, offlineUrl, name, phone, additionalId);
    }

    @Override
    protected void saveData(@NonNull UsedeskConfiguration configuration) {
        SharedPreferences.Editor editor = sharedPreferences.edit()
                .putString(KEY_ID, configuration.getCompanyId())
                .putString(KEY_URL, configuration.getUrl())
                .putString(KEY_OFFLINE_URL, configuration.getOfflineFormUrl())
                .putString(KEY_EMAIL, configuration.getEmail())
                .putString(KEY_NAME, configuration.getName());

        Long additionalId = configuration.getAdditionalId();
        if (additionalId != null) {
            editor.putLong(KEY_ADDITIONAL_ID, configuration.getAdditionalId());
        } else {
            editor.remove(KEY_ADDITIONAL_ID);
        }
        Long phone = configuration.getPhone();
        if (phone != null) {
            editor.putLong(KEY_PHONE, configuration.getPhone());
        } else {
            editor.remove(KEY_PHONE);
        }
        editor.apply();
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
