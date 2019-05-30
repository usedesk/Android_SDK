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
    private static final String KEY_EMAIL = "email";

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
        final String email = sharedPreferences.getString(KEY_EMAIL, null);

        if (id == null || url == null || email == null) {
            return null;
        }

        return new UsedeskConfiguration(id, email, url);
    }

    @Override
    protected void saveData(@NonNull UsedeskConfiguration configuration) {
        sharedPreferences.edit()
                .putString(KEY_ID, configuration.getCompanyId())
                .putString(KEY_URL, configuration.getUrl())
                .putString(KEY_EMAIL, configuration.getEmail())
                .apply();
    }

    @Override
    public void clearData() {
        super.clearData();

        sharedPreferences.edit()
                .remove(KEY_ID)
                .remove(KEY_URL)
                .remove(KEY_EMAIL)
                .apply();
    }
}
