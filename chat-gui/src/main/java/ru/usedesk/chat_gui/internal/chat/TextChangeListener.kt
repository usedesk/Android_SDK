package ru.usedesk.chat_gui.internal.chat;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;

class TextChangeListener implements TextWatcher {

    private final TextChangedAction textChangedAction;

    TextChangeListener(@NonNull TextChangedAction textChangedAction) {
        this.textChangedAction = textChangedAction;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        textChangedAction.onTextChanged(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    interface TextChangedAction {
        void onTextChanged(@NonNull String text);
    }
}
