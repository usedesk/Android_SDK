package ru.usedesk.sample.ui._common;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.widget.TextView;

import ru.usedesk.sample.ui.test.Model;

public class TextViewBinder<KEY> implements IUnbinder {
    private TextViewBinder(@NonNull LifecycleOwner lifecycleOwner,
                           @NonNull TextView textView,
                           @NonNull Model<KEY, ?> model,
                           @NonNull KEY keyText) {
        LiveData<String> textLiveData = model.getLiveData(keyText);
        textLiveData.observe(lifecycleOwner, textView::setText);
    }

    public static <KEY> IUnbinder bind(@NonNull LifecycleOwner lifecycleOwner,
                                       @NonNull TextView textView,
                                       @NonNull Model<KEY, ?> model,
                                       @NonNull KEY keyText) {
        return new TextViewBinder<>(lifecycleOwner, textView, model, keyText);
    }

    @Override
    public void unbind() {
    }
}
