package ru.usedesk.chat_sdk.external.entity;

public class UsedeskSingleLifeEvent extends UsedeskEvent {
    private boolean processed = false;

    public UsedeskSingleLifeEvent() {

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
