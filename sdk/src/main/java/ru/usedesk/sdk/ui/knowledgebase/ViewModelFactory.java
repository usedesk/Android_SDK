package ru.usedesk.sdk.ui.knowledgebase;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public abstract class ViewModelFactory<V> implements ViewModelProvider.Factory {

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) create();
    }

    @NonNull
    protected abstract V create();

    @NonNull
    protected abstract Class<V> getClassType();
}
