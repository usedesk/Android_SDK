package ru.usedesk.sample.ui._common;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {
    private final CompositeUnbinder compositeUnbinder = new CompositeUnbinder();

    protected void bindTextInput(@NonNull TextInputLayout layout, @NonNull IOnTextInputLiveData onTextInputLiveData) {
        compositeUnbinder.add(TextInputBinder.bind(this, layout, onTextInputLiveData));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        compositeUnbinder.unbind();
    }
}
