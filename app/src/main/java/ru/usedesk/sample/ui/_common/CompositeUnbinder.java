package ru.usedesk.sample.ui._common;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CompositeUnbinder {
    private final List<IUnbinder> unbinders = new ArrayList<>();

    public void add(@NonNull IUnbinder unbinder) {
        unbinders.add(unbinder);
    }

    public void unbind() {
        for (IUnbinder unbinder : unbinders) {
            unbinder.unbind();
        }
        unbinders.clear();
    }
}
