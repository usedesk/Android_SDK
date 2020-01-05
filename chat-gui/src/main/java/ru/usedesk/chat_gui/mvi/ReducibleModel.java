package ru.usedesk.chat_gui.mvi;

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
