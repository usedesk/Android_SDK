package ru.usedesk.chat_sdk.external.entity;

import androidx.annotation.Nullable;

public class UsedeskSingleLifeEvent<DATA> extends UsedeskEvent<DATA> {
    private boolean processed = false;

    public UsedeskSingleLifeEvent() {

    }

    public UsedeskSingleLifeEvent(@Nullable DATA data) {
        super(data);
    }

    @Override
    public boolean isProcessed() {
        return processed;
    }

    @Override
    public void setProcessed() {
        this.processed = true;
    }
}
