package ru.usedesk.common_gui.external;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IUsedeskViewCustomizer {

    void replaceId(int defaultId, int customId);

    @NonNull
    View createView(@NonNull ViewGroup viewGroup, int defaultId, int themeId);

    @NonNull
    View createView(@NonNull LayoutInflater inflater, int layoutId,
                    @Nullable ViewGroup viewGroup, boolean attachToRoot,
                    int themeId);
}
