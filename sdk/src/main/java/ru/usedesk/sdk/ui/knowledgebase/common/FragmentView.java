package ru.usedesk.sdk.ui.knowledgebase.common;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

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
