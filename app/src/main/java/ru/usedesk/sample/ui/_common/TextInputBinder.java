package ru.usedesk.sample.ui._common;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import ru.usedesk.sample.ui.test.Model;

public class TextInputBinder<KEY, INTENT> implements IUnbinder {
    private final Subject<String> textSubject = PublishSubject.create();

    @SuppressWarnings("ConstantConditions")
    private TextInputBinder(@NonNull LifecycleOwner lifecycleOwner,
                            @NonNull TextInputLayout layout,
                            @NonNull Model<KEY, INTENT> model,
                            @NonNull KEY keyText,
                            @NonNull KEY keyError,
                            @NonNull INTENT intent) {
        EditText editText = layout.getEditText();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textSubject.onNext(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        model.addIntent(intent, textSubject);

        //Последнее значение текстового поля
        String text = model.getValue(keyText);
        if (text != null) {
            editText.setText(text);
        }

        //Подписка на ошибки
        LiveData<String> errorObservable = model.getLiveData(keyError);
        errorObservable.observe(lifecycleOwner, layout::setError);
    }

    public static <KEY, INTENT> IUnbinder bind(@NonNull LifecycleOwner lifecycleOwner,
                                               @NonNull TextInputLayout layout,
                                               @NonNull Model<KEY, INTENT> model,
                                               @NonNull KEY keyText,
                                               @NonNull KEY keyError,
                                               @NonNull INTENT intent) {
        return new TextInputBinder<>(lifecycleOwner, layout, model, keyText, keyError, intent);
    }

    @Override
    public void unbind() {
        textSubject.onComplete();
    }
}
