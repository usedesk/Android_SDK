package ru.usedesk.sample.ui._common;

public class ReusableEvent<DATA> extends Event<DATA> {

    public ReusableEvent(DATA data) {
        super(data);
    }

    @Override
    public void onProcessed() {

    }

    @Override
    public boolean isProcessed() {
        return false;
    }
}
