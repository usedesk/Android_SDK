package ru.usedesk.sample.ui._common;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

public class TextInputLiveData {
    private final MutableLiveData<String> textLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    @NonNull
    public LiveData<String> getTextLiveData() {
        return textLiveData;
    }

    @NonNull
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void postText(@NonNull String text) {
        textLiveData.postValue(text);
    }

    public void postError(@NonNull String error) {
        errorLiveData.postValue(error);
    }

    public void post(@NonNull String text, @NonNull String error) {
        postText(text);
        postError(error);
    }

    public void setText(@NonNull String initValue) {
        textLiveData.setValue(initValue);
    }
}
