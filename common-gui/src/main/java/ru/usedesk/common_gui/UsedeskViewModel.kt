package ru.usedesk.common_gui

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.updateAndGet

open class UsedeskViewModel<MODEL>(
    defaultModel: MODEL
) : ViewModel() {
    private val _modelFlow = MutableStateFlow(defaultModel)
    val modelFlow: StateFlow<MODEL> = _modelFlow
    private val mainThread = AndroidSchedulers.mainThread()

    private val disposables = mutableListOf<Disposable>()
    private var inited = false

    protected val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    protected val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    protected fun doInit(init: () -> Unit) {
        if (!inited) {
            inited = true
            init()
        }
    }

    protected fun setModel(onUpdate: MODEL.() -> MODEL) = _modelFlow.updateAndGet { it.onUpdate() }

    protected fun <T> T.update(onUpdate: T.() -> T): T = onUpdate()

    @Deprecated("Migrate to coroutines")
    private fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    @Deprecated("Migrate to coroutines")
    protected fun doIt(
        completable: Completable,
        onCompleted: () -> Unit = {},
        onThrowable: (Throwable) -> Unit = Throwable::printStackTrace
    ) = completable.observeOn(mainThread)
        .subscribe({ onCompleted() }, { onThrowable(it) })
        .also(this::addDisposable)

    @Deprecated("Migrate to coroutines")
    private fun <T> doIt(
        observable: Observable<T>,
        onValue: (T) -> Unit = {},
        onThrowable: (Throwable) -> Unit = Throwable::printStackTrace
    ) = observable.observeOn(mainThread)
        .subscribe({ onValue(it) }, { onThrowable(it) })
        .also(this::addDisposable)

    @Deprecated("Migrate to coroutines")
    protected fun <T> doIt(
        single: Single<T>,
        onValue: (T) -> Unit = {},
        onThrowable: (Throwable) -> Unit = Throwable::printStackTrace
    ) = doIt(single.toObservable(), onValue, onThrowable)

    @Deprecated("Migrate to coroutines")
    @SuppressLint("CheckResult")
    protected fun justDoIt(
        completable: Completable,
        onSuccess: () -> Unit = {},
        onThrowable: (Throwable) -> Unit = Throwable::printStackTrace
    ) {
        completable.observeOn(mainThread)
            .subscribe(onSuccess, onThrowable)
    }

    override fun onCleared() {
        super.onCleared()

        disposables.forEach(Disposable::dispose)

        ioScope.cancel()
        mainScope.cancel()
    }
}