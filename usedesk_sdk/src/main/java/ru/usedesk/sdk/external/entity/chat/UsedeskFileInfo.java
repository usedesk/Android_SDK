package ru.usedesk.sdk.external.entity.chat;

import android.net.Uri;
import android.support.annotation.NonNull;

public class UsedeskFileInfo {
    private final Uri uri;

    public UsedeskFileInfo(@NonNull Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }
}
