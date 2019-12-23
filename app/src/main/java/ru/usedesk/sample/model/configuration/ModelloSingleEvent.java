package ru.usedesk.sample.model.configuration;

public class ModelloSingleEvent extends ModelloEvent {
    private boolean processed = false;

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed() {
        this.processed = true;
    }
}
