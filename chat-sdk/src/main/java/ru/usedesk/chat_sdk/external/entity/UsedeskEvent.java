package ru.usedesk.chat_sdk.external.entity;

import androidx.annotation.Nullable;

public class UsedeskEvent<DATA> {
    private final DATA data;

    public UsedeskEvent() {
        this(null);
    }

    public UsedeskEvent(@Nullable DATA data) {
        this.data = data;
    }

    @Nullable
    public DATA getData() {
        return data;
    }

    public boolean isProcessed() {
        return false;
    }

    public void setProcessed() {

    }
}
