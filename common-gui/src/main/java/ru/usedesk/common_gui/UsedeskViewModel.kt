package ru.usedesk.common_gui

import android.annotation.SuppressLint
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

open class UsedeskViewModel<MODEL>(
    defaultModel: MODEL
) : ViewModel() {
    val modelLiveData = UsedeskLiveData(defaultModel)
    val mainThread = AndroidSchedulers.mainThread()

    private val disposables = mutableListOf<Disposable>()
    private var inited = false

    protected fun doInit(init: () -> Unit) {
        if (!inited) {
            inited = true
            init()
        }
    }

    @MainThread
    protected fun setModel(onUpdate: (MODEL) -> MODEL) {
        modelLiveData.value = onUpdate(modelLiveData.value)
    }

    protected fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    protected fun doIt(
        completable: Completable,
        onCompleted: () -> Unit = {},
        onThrowable: (Throwable) -> Unit = { throwable(it) }
    ): Disposable {
        return completable.observeOn(mainThread)
            .subscribe({ onCompleted() }, { onThrowable(it) }).also {
                addDisposable(it)
            }
    }

    protected fun doIt(
        completableList: List<Completable>,
        onCompleted: () -> Unit = {},
        onThrowable: (Throwable) -> Unit = { throwable(it) }
    ): Disposable {
        val completable = Completable.concat(completableList)
            .observeOn(mainThread)
        return doIt(completable, onCompleted, onThrowable)
    }

    protected fun <T> doIt(
        observable: Observable<T>,
        onValue: (T) -> Unit = {},
        onThrowable: (Throwable) -> Unit = { throwable(it) }
    ): Disposable {
        return observable.observeOn(mainThread)
            .subscribe({ onValue(it) }, { onThrowable(it) }).also {
                addDisposable(it)
            }
    }

    protected fun <T> doIt(
        single: Single<T>,
        onValue: (T) -> Unit = {},
        onThrowable: (Throwable) -> Unit = { throwable(it) }
    ): Disposable {
        return doIt(single.toObservable(), onValue, onThrowable)
    }

    @SuppressLint("CheckResult")
    protected fun justDoIt(
        completable: Completable,
        onSuccess: () -> Unit = {},
        onThrowable: (Throwable) -> Unit = { throwable(it) }
    ) {
        completable.observeOn(mainThread)
            .subscribe(onSuccess, onThrowable)
    }

    @SuppressLint("CheckResult")
    protected fun <T> justDoIt(
        single: Single<T>,
        onResult: (T) -> Unit = {},
        onThrowable: (Throwable) -> Unit = { throwable(it) }
    ) {
        single.observeOn(mainThread)
            .subscribe(onResult, onThrowable)
    }

    @SuppressLint("CheckResult")
    protected fun <T> justDoIt(
        observable: Observable<T>,
        onResult: (T) -> Unit = {},
        onThrowable: (Throwable) -> Unit = { throwable(it) }
    ) {
        observable.observeOn(mainThread)
            .subscribe(onResult, onThrowable)
    }

    private fun throwable(throwable: Throwable) {
        throwable.printStackTrace()
    }

    override fun onCleared() {
        super.onCleared()

        disposables.forEach {
            it.dispose()
        }
    }
}