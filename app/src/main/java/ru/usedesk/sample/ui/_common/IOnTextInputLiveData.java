package ru.usedesk.sample.ui._common;

import android.support.annotation.NonNull;

import io.reactivex.Observable;

public interface IOnTextInputLiveData {
    @NonNull
    TextInputLiveData fromObservable(@NonNull Observable<String> textObservable);
}
