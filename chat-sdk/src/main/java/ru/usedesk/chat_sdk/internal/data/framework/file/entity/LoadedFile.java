package ru.usedesk.chat_sdk.internal.data.framework.file.entity;

import androidx.annotation.NonNull;

public class LoadedFile {
    String name;
    int size;
    String type;
    byte[] bytes;

    public LoadedFile(@NonNull String name,
                      int size,
                      @NonNull String type,
                      byte[] bytes) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.bytes = bytes;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
