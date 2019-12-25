package ru.usedesk.sdk.internal.data.framework.api.standard.entity.request;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SetEmailRequest extends BaseRequest {

    public static final String TYPE = "@@server/chat/SET_EMAIL";

    private final Payload payload;
    private final String email;

    public SetEmailRequest(@NonNull String token, @NonNull String email, @Nullable String name,
                           @Nullable Long phone, @Nullable Long additionalId) {
        super(TYPE, token);
        this.email = email;
        this.payload = new Payload(email, name, phone, additionalId);
    }

    class Payload {
        private final String email;
        private final String name;
        private final Long phone;
        private final Long additionalId;

        Payload(@NonNull String email, @Nullable String name, @Nullable Long phone, @Nullable Long additionalId) {
            this.email = email;
            this.name = name;
            this.phone = phone;
            this.additionalId = additionalId;
        }
    }
}