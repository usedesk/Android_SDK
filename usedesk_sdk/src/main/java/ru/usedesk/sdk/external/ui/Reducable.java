package ru.usedesk.sdk.external.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class Reducable<T> {

    protected static <T> T reduceValue(@NonNull T left, @Nullable T right) {
        return right != null
                ? right
                : left;
    }

    @NonNull
    public abstract T reduce(@NonNull T oldModel);
}
