package ru.usedesk.chat_gui.internal._extra

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_gui.internal._extra.utils.dispose
import ru.usedesk.chat_gui.internal._extra.utils.throwable
import java.util.*

open class UsedeskViewModel : ViewModel() {
    private val disposables: MutableList<Disposable> = LinkedList()
    private var inited = false

    protected fun doInit(init: () -> Unit) {
        if (!inited) {
            inited = true
            init()
        }
    }

    private fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    protected fun doIt(completable: Completable,
                       onCompleted: () -> Unit = {},
                       onThrowable: (Throwable) -> Unit = { throwable(it) }) {
        addDisposable(completable.observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onCompleted() }, { onThrowable(it) }))
    }

    protected fun doIt(completableList: List<Completable>,
                       onCompleted: () -> Unit = {},
                       onThrowable: (Throwable) -> Unit = { throwable(it) }) {
        val completable = Completable.concat(completableList)
                .observeOn(AndroidSchedulers.mainThread())
        doIt(completable, onCompleted, onThrowable)
    }

    protected fun <T> doIt(observable: Observable<T>,
                           onValue: (T) -> Unit = {},
                           onThrowable: (Throwable) -> Unit = { throwable(it) }) {
        addDisposable(observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onValue(it) }, { onThrowable(it) }))
    }

    protected fun <T> doIt(single: Single<T>,
                           onValue: (T) -> Unit = {},
                           onThrowable: (Throwable) -> Unit = { throwable(it) }) {
        doIt(single.toObservable(), onValue, onThrowable)
    }

    @SuppressLint("CheckResult")
    protected fun justDoIt(completable: Completable,
                           onSuccess: () -> Unit = {},
                           onThrowable: (Throwable) -> Unit = { throwable(it) }) {
        completable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess, onThrowable)
    }

    @SuppressLint("CheckResult")
    protected fun <T> justDoIt(single: Single<T>,
                               onResult: (T) -> Unit = {},
                               onThrowable: (Throwable) -> Unit = { throwable(it) }) {
        single.observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResult, onThrowable)
    }

    @SuppressLint("CheckResult")
    protected fun <T> justDoIt(observable: Observable<T>,
                               onResult: (T) -> Unit = {},
                               onThrowable: (Throwable) -> Unit = { throwable(it) }) {
        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResult, onThrowable)
    }

    override fun onCleared() {
        super.onCleared()
        for (disposable in disposables) {
            dispose(disposable)
        }
    }
}