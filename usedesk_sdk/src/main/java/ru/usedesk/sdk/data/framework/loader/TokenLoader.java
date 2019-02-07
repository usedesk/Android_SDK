package ru.usedesk.sdk.data.framework.loader;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.usedesk.sdk.data.repository.user.info.DataLoader;

@Singleton
public class TokenLoader extends DataLoader<String> {
    private static final String PREF_NAME = "usedeskSdkToken";
    private static final String KEY_TOKEN = "token";//TODO: replace

    private final SharedPreferences sharedPreferences;

    @Inject
    public TokenLoader(@NonNull Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
        super.clearData();

        sharedPreferences.edit()
                .remove(KEY_TOKEN)
                .apply();
    }
}
