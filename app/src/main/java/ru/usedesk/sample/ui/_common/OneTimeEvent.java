package ru.usedesk.sample.ui._common;

public class OneTimeEvent<DATA> extends Event<DATA> {
    private boolean processed = false;

    public OneTimeEvent(DATA data) {
        super(data);
    }

    @Override
    public void onProcessed() {
        processed = true;
    }

    @Override
    public boolean isProcessed() {
        return processed;
    }
}
