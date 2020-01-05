package ru.usedesk.common_gui.external;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface IUsedeskViewCustomizer {

    int getLayoutId(int defaultId);

    void setLayoutId(int defaultId, int customId);

    void setThemeId(int themeId);

    @NonNull
    View createView(@NonNull ViewGroup viewGroup, int defaultId);

    @NonNull
    View createView(@NonNull LayoutInflater inflater, int layoutId,
                    @Nullable ViewGroup viewGroup, boolean attachToRoot);
}
