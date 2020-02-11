package ru.usedesk.chat_sdk.external.entity;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UsedeskFileInfo {
    private final Uri uri;
    private final Type type;

    public UsedeskFileInfo(@NonNull Uri uri, @NonNull Type type) {
        this.uri = uri;
        this.type = type;
    }

    @NonNull
    public Uri getUri() {
        return uri;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    public enum Type {
        IMAGE,
        VIDEO,
        DOCUMENT,
        OTHER;

        public static Type getByMimeType(@Nullable String mimeType) {
            if (mimeType != null) {
                if (mimeType.startsWith("image/")) {
                    return IMAGE;
                } else if (mimeType.startsWith("video/")) {
                    return VIDEO;
                } else if (mimeType.startsWith("doc/")) {
                    return DOCUMENT;
                }
            }
            return OTHER;
        }
    }
}
