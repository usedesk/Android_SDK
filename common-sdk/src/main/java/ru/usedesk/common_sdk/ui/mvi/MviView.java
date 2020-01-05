package ru.usedesk.common_sdk.ui.mvi;

import android.support.annotation.NonNull;

public interface MviView<M> {
    @NonNull
    void renderModel(@NonNull M model);
}
