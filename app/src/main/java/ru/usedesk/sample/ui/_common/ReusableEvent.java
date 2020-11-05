package ru.usedesk.sample.ui._common;

public class ReusableEvent<DATA> extends Event<DATA> {

    public ReusableEvent(DATA data) {
        super(data);
    }

    @Override
    public void doEvent(IDoIt<DATA> doIt) {
        doIt.doId(getData());
    }
}
