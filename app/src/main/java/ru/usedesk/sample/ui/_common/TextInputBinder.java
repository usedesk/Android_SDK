package ru.usedesk.sample.ui._common;

import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class TextInputBinder implements IUnbinder {
    private final Subject<String> textSubject = PublishSubject.create();

    @SuppressWarnings("ConstantConditions")
    private TextInputBinder(@NonNull LifecycleOwner lifecycleOwner,
                            @NonNull TextInputLayout layout,
                            @NonNull IOnTextInputLiveData layoutLiveData) {
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

        //Последнее значение текстового поля
        TextInputLiveData textInputLiveData = layoutLiveData.fromObservable(textSubject);
        String text = textInputLiveData.getTextLiveData().getValue();
        if (text != null) {
            editText.setText(text);
        }

        //Подписка на ошибки
        textInputLiveData.getErrorLiveData()
                .observe(lifecycleOwner, layout::setError);
    }

    public static IUnbinder bind(@NonNull LifecycleOwner lifecycleOwner,
                                 @NonNull TextInputLayout layout,
                                 @NonNull IOnTextInputLiveData layoutLiveData) {
        return new TextInputBinder(lifecycleOwner, layout, layoutLiveData);
    }

    @Override
    public void unbind() {
        textSubject.onComplete();
    }
}
