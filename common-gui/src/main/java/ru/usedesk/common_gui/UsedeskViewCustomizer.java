package ru.usedesk.common_gui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseIntArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class UsedeskViewCustomizer {

    private final SparseIntArray layoutIds = new SparseIntArray();

    private int themeId = R.style.Usedesk_Theme;

    public UsedeskViewCustomizer() {
    }

    public View createView(@NonNull ViewGroup viewGroup, int defaultId) {
        return createView(LayoutInflater.from(viewGroup.getContext()),
                getLayoutId(defaultId), viewGroup, false);
    }

    public int getLayoutId(int defaultId) {
        return layoutIds.get(defaultId, defaultId);
    }

    public void setLayoutId(int defaultId, int customId) {
        layoutIds.put(defaultId, customId);
    }

    public void setThemeId(int themeId) {
        this.themeId = themeId;
    }

    @NonNull
    public View createView(@NonNull LayoutInflater inflater, int layoutId,
                           @Nullable ViewGroup viewGroup, boolean attachToRoot) {
        Context contextThemeWrapper = new ContextThemeWrapper(inflater.getContext(), themeId);
        LayoutInflater layoutInflater = inflater.cloneInContext(contextThemeWrapper);
        return layoutInflater.inflate(layoutId, viewGroup, attachToRoot);
    }
}
