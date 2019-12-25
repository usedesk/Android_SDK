package ru.usedesk.sample.ui._common;

public abstract class Event<DATA> {
    private final DATA data;

    public Event(DATA data) {
        this.data = data;
    }

    public DATA getData() {
        return data;
    }

    abstract public boolean isProcessed();

    abstract public void onProcessed();
}
