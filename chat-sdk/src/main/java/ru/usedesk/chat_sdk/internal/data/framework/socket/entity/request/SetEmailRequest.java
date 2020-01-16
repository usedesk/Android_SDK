package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SetEmailRequest extends BaseRequest {

    private static final String TYPE = "@@server/chat/SET_EMAIL";

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