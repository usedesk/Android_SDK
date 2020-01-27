package ru.usedesk.knowledgebase_gui.internal.screens.common;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

public abstract class FragmentView<T extends ViewModel> extends Fragment {

    private T viewModel;

    public void initViewModel(@NonNull ViewModelFactory<T> viewModelFactory) {
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(viewModelFactory.getClassType());
    }

    @NonNull
    protected T getViewModel() {
        if (viewModel == null) {
            throw new RuntimeException("You must call initViewModel before");
        }
        return viewModel;
    }

    @NonNull
    protected Bundle getNonNullArguments() {
        if (getArguments() == null) {
            throw new RuntimeException("You must call newInstance method");
        }
        return getArguments();
    }
}
