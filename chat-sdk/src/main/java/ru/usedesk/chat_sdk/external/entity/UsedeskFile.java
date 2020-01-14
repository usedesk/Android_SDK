package ru.usedesk.chat_sdk.external.entity;

import android.support.annotation.NonNull;

public class UsedeskFile {

    private static final String IMAGE_TYPE = "image/";

    private String content;
    private String type;
    private String size;
    private String name;

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
        return type != null && type.startsWith(IMAGE_TYPE) ||
                name != null && endsLikeImage();
    }

    private boolean endsLikeImage() {
        return name.endsWith(".png") || name.endsWith(".jpg") ||
                name.endsWith(".bmp") || name.endsWith(".jpeg");
    }
}