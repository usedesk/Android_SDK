package ru.usedesk.sdk.external.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

public class ViewCustomizer {

    private final SparseIntArray layoutIds = new SparseIntArray();

    @Inject
    public ViewCustomizer() {
    }

    public View createView(@NonNull ViewGroup viewGroup, int defaultId) {
        int resourceId = getLayoutId(defaultId);

        return LayoutInflater.from(viewGroup.getContext())
                .inflate(resourceId, viewGroup, false);
    }

    public int getLayoutId(int defaultId) {
        return layoutIds.get(defaultId, defaultId);
    }

    public void setLayoutId(int defaultId, int customId) {
        layoutIds.put(defaultId, customId);
    }

    @NonNull
    public View createView(@NonNull LayoutInflater inflater, int layoutId,
                           @Nullable ViewGroup viewGroup, boolean attachToRoot) {
        return inflater.inflate(layoutId, viewGroup, attachToRoot);
    }
}
