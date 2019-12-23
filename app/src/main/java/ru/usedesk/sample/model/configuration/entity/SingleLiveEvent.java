package ru.usedesk.sample.model.configuration.entity;

import android.support.annotation.NonNull;

public class SingleLiveEvent extends SingleLiveData<Object> {
    @SuppressWarnings("ConstantConditions")
    public SingleLiveEvent() {
        super(null);
    }

    @NonNull
    @Override
    public Object getData() {
        throw new RuntimeException("This class has not data");
    }
}
