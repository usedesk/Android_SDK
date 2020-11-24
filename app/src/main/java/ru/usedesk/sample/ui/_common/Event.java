package ru.usedesk.sample.ui._common;

public abstract class Event<DATA> {
    private final DATA data;

    public Event(DATA data) {
        this.data = data;
    }

    public DATA getData() {
        return data;
    }

    abstract public void doEvent(IDoIt<DATA> doIt);

    public interface IDoIt<DATA> {
        void doId(DATA data);
    }
}
