package ru.usedesk.common_gui.internal.ui.mvi;

import android.support.annotation.NonNull;

public interface MviView<M> {
    @NonNull
    void renderModel(@NonNull M model);
}
