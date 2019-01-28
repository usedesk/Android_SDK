package ru.usedesk.sdk.data.framework;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

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

    }
}
