package ru.usedesk.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Set;

public class SharedHelper {

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor sharedPreferencesEditor;

    public SharedHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.NAME, Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    private void delete(String key) {
        if (sharedPreferences.contains(key)) {
            sharedPreferencesEditor.remove(key).commit();
        }
    }

    private void saveString(String key, String value) {
        delete(key);
        sharedPreferencesEditor.putString(key, value);
        sharedPreferencesEditor.commit();
    }

    private void saveInt(String key, int value) {
        delete(key);
        sharedPreferencesEditor.putInt(key, value);
        sharedPreferencesEditor.commit();
    }

    private void saveLong(String key, long value) {
        delete(key);
        sharedPreferencesEditor.putLong(key, value);
        sharedPreferencesEditor.commit();
    }

    private void saveBoolean(String key, Boolean value) {
        delete(key);
        sharedPreferencesEditor.putBoolean(key, value);
        sharedPreferencesEditor.commit();
    }

    private void saveStringSet(String key, Set<String> value) {
        delete(key);
        sharedPreferencesEditor.putStringSet(key, value);
        sharedPreferencesEditor.commit();
    }

    private String getString(String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    private int getInt(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }

    private long getLong(String key, int defValue) {
        return sharedPreferences.getLong(key, defValue);
    }

    private Boolean getBoolean(String key, Boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    private Set<String> getStringSet(String key, Set<String> defValue) {
        return sharedPreferences.getStringSet(key, defValue);
    }

    // --- data ---
    public void clearAll() {
        sharedPreferencesEditor.clear().commit();
    }

    public String getToken() {
        return getString(Constants.PREF_TOKEN, null);
    }

    public void saveToken(String value) {
        saveString(Constants.PREF_TOKEN, value);
    }

    public boolean changedEmail(String newEmail) {
        String savedEmail = getEmail();
        return !TextUtils.isEmpty(savedEmail) && !newEmail.equals(savedEmail);
    }

    public String getUrl() {
        return getString(Constants.PREF_URL, null);
    }

    public void saveUrl(String value) {
        saveString(Constants.PREF_URL, value);
    }

    public String getEmail() {
        return getString(Constants.PREF_EMAIL, null);
    }

    public void saveEmail(String value) {
        saveString(Constants.PREF_EMAIL, value);
    }

    static class Constants {

        static final String NAME = "usedesk_sdk";
        static final String PREF_TOKEN = "token";
        static final String PREF_URL = "url";
        static final String PREF_EMAIL = "email";
    }
}