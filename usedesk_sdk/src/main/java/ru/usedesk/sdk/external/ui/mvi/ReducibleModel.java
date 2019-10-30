package ru.usedesk.sdk.external.ui.mvi;

import android.support.annotation.NonNull;

public abstract class ReducibleModel<T> {

    protected static <T> T reduce(T oldValue, T newValue) {
        return newValue != null
                ? newValue
                : oldValue;
    }

    @NonNull
    public abstract T reduce(@NonNull T oldModel);
}
