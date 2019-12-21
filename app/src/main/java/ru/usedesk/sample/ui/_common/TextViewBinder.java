package ru.usedesk.sample.ui._common;

import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;
import android.widget.TextView;

public class TextViewBinder implements IUnbinder {
    private TextViewBinder(@NonNull LifecycleOwner lifecycleOwner,
                           @NonNull TextView textView,
                           @NonNull IOnTextLiveData onTextLiveData) {
        onTextLiveData.getLiveData().observe(lifecycleOwner, textView::setText);
    }

    public static IUnbinder bind(@NonNull LifecycleOwner lifecycleOwner,
                                 @NonNull TextView textView,
                                 @NonNull IOnTextLiveData onTextLiveData) {
        return new TextViewBinder(lifecycleOwner, textView, onTextLiveData);
    }

    @Override
    public void unbind() {
    }
}
