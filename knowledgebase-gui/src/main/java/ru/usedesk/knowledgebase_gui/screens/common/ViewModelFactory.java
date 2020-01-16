package ru.usedesk.knowledgebase_gui.screens.common;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

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
