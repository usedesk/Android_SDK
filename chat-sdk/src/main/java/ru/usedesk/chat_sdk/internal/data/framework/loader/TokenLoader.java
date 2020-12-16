package ru.usedesk.chat_sdk.internal.data.framework.loader;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import ru.usedesk.chat_sdk.internal.data.framework.info.DataLoader;

@Singleton
public class TokenLoader extends DataLoader<String> {
    private static final String PREF_NAME = "usedeskSdkToken";
    private static final String KEY_TOKEN = "token";

    private final SharedPreferences sharedPreferences;

    @Inject
    @Named("token")
    TokenLoader(@NonNull Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_MULTI_PROCESS);
    }

    @Nullable
    @Override
    protected String loadData() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    @Override
    protected void saveData(@NonNull String token) {
        sharedPreferences.edit()
                .putString(KEY_TOKEN, token)
                .apply();
    }

    @Override
    public void clearData() {
        sharedPreferences.edit()
                .remove(KEY_TOKEN)
                .apply();
    }
}
