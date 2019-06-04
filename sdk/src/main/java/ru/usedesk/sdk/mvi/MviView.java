package ru.usedesk.sdk.mvi;

import android.support.annotation.NonNull;

public interface MviView<M> {
    @NonNull
    void renderModel(@NonNull M model);
}
