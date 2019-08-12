package ru.usedesk.sdk.external.ui;

import android.support.annotation.NonNull;

public interface Reducable<T> {
    @NonNull
    T reduce(@NonNull T oldModel);
}
