package ru.usedesk.sample.ui._common;

public class OneTimeEvent implements Event {
    private boolean processed = false;

    @Override
    public void onProcessed() {
        processed = true;
    }

    @Override
    public boolean isProcessed() {
        return processed;
    }
}
