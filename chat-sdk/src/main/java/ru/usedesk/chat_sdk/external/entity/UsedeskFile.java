package ru.usedesk.chat_sdk.external.entity;

import android.support.annotation.NonNull;

public class UsedeskFile {

    private static final String IMAGE_TYPE = "image/";

    private final String content;
    private final String type;
    private final String size;
    private final String name;

    public UsedeskFile(@NonNull String content, @NonNull String type, String size, @NonNull String name) {
        this.content = content;
        this.type = type;
        this.size = size;
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public String getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public boolean isImage() {
        return type.startsWith(IMAGE_TYPE) || endsLikeImage();
    }

    private boolean endsLikeImage() {
        return name.endsWith(".png") || name.endsWith(".jpg") ||
                name.endsWith(".bmp") || name.endsWith(".jpeg");
    }
}