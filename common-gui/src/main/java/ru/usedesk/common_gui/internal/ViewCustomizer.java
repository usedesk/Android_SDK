package ru.usedesk.common_gui.internal;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.inject.Inject;

import ru.usedesk.common_gui.external.IUsedeskViewCustomizer;

public class ViewCustomizer implements IUsedeskViewCustomizer {

    private final SparseIntArray layoutIds = new SparseIntArray();

    @Inject
    ViewCustomizer() {
    }

    public int getId(int defaultId) {
        return layoutIds.get(defaultId);
    }

    @Override
    public void replaceId(int defaultId, int customId) {
        layoutIds.put(defaultId, customId);
    }

    @NonNull
    @Override
    public View createView(@NonNull ViewGroup viewGroup, int defaultId, int themeId) {
        return createView(LayoutInflater.from(viewGroup.getContext()),
                getId(defaultId), viewGroup, false, themeId);
    }


    @Override
    @NonNull
    public View createView(@NonNull LayoutInflater inflater, int layoutId,
                           @Nullable ViewGroup viewGroup, boolean attachToRoot,
                           int themeId) {
        Context contextThemeWrapper = new ContextThemeWrapper(inflater.getContext(), getId(themeId));
        LayoutInflater layoutInflater = inflater.cloneInContext(contextThemeWrapper);
        return layoutInflater.inflate(layoutId, viewGroup, attachToRoot);
    }

}
