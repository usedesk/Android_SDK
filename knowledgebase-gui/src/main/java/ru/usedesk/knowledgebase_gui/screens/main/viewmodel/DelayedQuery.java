package ru.usedesk.knowledgebase_gui.screens.main.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

class DelayedQuery {
    private final PublishSubject<String> queryPublishSubject = PublishSubject.create();
    private final Disposable disposable;

    DelayedQuery(@NonNull final MutableLiveData<String> searchQueryLiveData, int delayMilliseconds) {
        disposable = queryPublishSubject.debounce(delayMilliseconds, TimeUnit.MILLISECONDS)
                .subscribe(searchQueryLiveData::postValue);
    }

    void dispose() {
        disposable.dispose();
    }

    void onNext(String query) {
        queryPublishSubject.onNext(query);
    }
}
