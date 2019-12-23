package ru.usedesk.sample.model.configuration.entity;

import android.support.annotation.NonNull;

public class SingleLiveData<DATA> {
    private final DATA data;
    private boolean processed = false;

    public SingleLiveData(@NonNull DATA data) {
        this.data = data;
    }

    @NonNull
    public DATA getData() {
        return data;
    }

    public void setProcessed() {
        this.processed = true;
    }

    public boolean isProcessed() {
        return processed;
    }
}
