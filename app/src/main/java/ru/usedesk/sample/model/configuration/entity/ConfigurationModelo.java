package ru.usedesk.sample.model.configuration.entity;

import android.support.annotation.NonNull;

public class ConfigurationModelo extends Modelo<ConfigurationModelo.DataKey, ConfigurationModelo.IntentKey> {

    public ConfigurationModelo() {
        super(ConfigurationModelo.DataKey.values(), IntentKey.values());
    }

    public enum IntentKey {
        EDIT_COMPANY_ID,
        EDIT_EMAIL,
        EDIT_URL,
        EDIT_OFFLINE_FORM_URL,
        EDIT_ACCOUNT_ID,
        EDIT_TOKEN,
        EDIT_CLIENT_NAME,
        EDIT_CLIENT_PHONE_NUMBER,
        EDIT_CLIENT_ADDITIONAL_ID,
        SET_FOREGROUND_SERVICE,
        SET_CUSTOM_VIEWS,
        SET_WITH_KNOWLEDGE_BASE,
        EVENT_SET_CONFIGURATION,
        EVENT_INIT_CONFIGURATION
    }

    public enum DataKey implements Modelo.DataKey {
        COMPANY_ID(""),
        EMAIL(""),
        URL(""),
        OFFLINE_FORM_URL(""),
        ACCOUNT_ID(""),
        TOKEN(""),
        CLIENT_NAME(""),
        CLIENT_PHONE_NUMBER(""),
        CLIENT_ADDITIONAL_ID(""),
        FOREGROUND_SERVICE(false),
        CUSTOM_VIEWS(false),
        WITH_KNOWLEDGE_BASE(true),
        COMPANY_ID_ERROR(""),
        EMAIL_ERROR(""),
        URL_ERROR(""),
        OFFLINE_FORM_URL_ERROR(""),
        ACCOUNT_ID_ERROR(""),
        TOKEN_ERROR(""),
        EVENT_SET_CONFIGURATION("");

        private final Object value;

        DataKey(@NonNull Object value) {
            this.value = value;
        }

        @Override
        @NonNull
        public Object getDefault() {
            return value;
        }
    }
}
