package ru.usedesk.knowledgebase_gui.internal.screens.main

import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

internal class DelayedQuery(
        searchQueryLiveData: MutableLiveData<String>,
        delayMilliseconds: Int
) {

    private val queryPublishSubject = PublishSubject.create<String>()
    private val disposable: Disposable = queryPublishSubject.debounce(
            delayMilliseconds.toLong(),
            TimeUnit.MILLISECONDS
    ).subscribe {
        searchQueryLiveData.postValue(it)
    }

    fun dispose() {
        disposable.dispose()
    }

    fun onNext(query: String) {
        queryPublishSubject.onNext(query)
    }
}