package ru.usedesk.sdk.internal.data.framework.loader;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import ru.usedesk.sdk.external.entity.knowledgebase.KnowledgeBaseConfiguration;
import ru.usedesk.sdk.internal.data.repository.user.info.DataLoader;

public class KnowledgeBaseConfigurationLoader extends DataLoader<KnowledgeBaseConfiguration> {
    private static final String PREF_NAME = "knowledgeBaseConfigurationPref";
    private static final String KEY_TOKEN = "tokenKey";
    private static final String KEY_COMPANY_ID = "companyIdKey";

    private final SharedPreferences sharedPreferences;

    @Inject
    KnowledgeBaseConfigurationLoader(@NonNull Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    protected KnowledgeBaseConfiguration loadData() {
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        String companyId = sharedPreferences.getString(KEY_COMPANY_ID, null);
        if (token != null && companyId != null) {
            return new KnowledgeBaseConfiguration(companyId, token);
        }
        return null;
    }

    @Override
    protected void saveData(@NonNull KnowledgeBaseConfiguration configuration) {
        sharedPreferences.edit()
                .putString(KEY_TOKEN, configuration.getToken())
                .putString(KEY_COMPANY_ID, configuration.getAccountId())
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
