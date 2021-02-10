package ru.usedesk.common_gui.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import ru.usedesk.common_gui.external.IUsedeskViewCustomizer;

public class ViewCustomizer implements IUsedeskViewCustomizer {

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Integer> layoutIds = new HashMap<>();

    @Inject
    ViewCustomizer() {
    }

    public int getId(int defaultId) {
        Integer id = layoutIds.get(defaultId);
        return id == null
                ? defaultId
                : id;
    }

    @Override
    public void replaceId(int defaultId, int customId) {
        layoutIds.put(defaultId, customId);
    }

    @NonNull
    @Override
    public View createView(@NonNull ViewGroup viewGroup, int defaultId, int themeId) {
        return createView(LayoutInflater.from(viewGroup.getContext()),
                getId(defaultId), viewGroup, false, getId(themeId));
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
