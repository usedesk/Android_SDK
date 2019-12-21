package ru.usedesk.sample.ui._common;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import ru.usedesk.sample.ui.test.Model;

public class BaseFragment extends Fragment {
    private final CompositeUnbinder compositeUnbinder = new CompositeUnbinder();

    protected <KEY, INTENT> void bindTextInput(@NonNull TextInputLayout layout, @NonNull Model<KEY, INTENT> model,
                                               @NonNull KEY keyText, @NonNull KEY keyError,
                                               @NonNull INTENT intent) {
        compositeUnbinder.add(TextInputBinder.bind(this, layout, model, keyText, keyError, intent));
    }

    protected <KEY> void bindTextView(@NonNull TextView textView, @NonNull Model<KEY, ?> model, @NonNull KEY key) {
        compositeUnbinder.add(TextViewBinder.bind(this, textView, model, key));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        compositeUnbinder.unbind();
    }
}
