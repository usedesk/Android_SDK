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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class UsedeskViewModel<MODEL>(
    defaultModel: MODEL
) : ViewModel() {
    val modelFlow = MutableStateFlow(defaultModel)
    private val mainThread = AndroidSchedulers.mainThread()

    private val mutex = Mutex()

    private val disposables = mutableListOf<Disposable>()
    private var inited = false

    private val jobs = mutableListOf<Job>()
    private val jobMutex = Mutex()

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val mainScope = CoroutineScope(Dispatchers.Main)

    private fun doSuspend(scope: CoroutineScope, onDo: suspend () -> Unit) = runBlocking {
        jobMutex.withLock {
            scope.launch {
                try {
                    onDo()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.also(jobs::add)
        }
    }

    fun doMain(onDo: suspend () -> Unit) = doSuspend(mainScope, onDo)

    fun doIo(onDo: suspend () -> Unit) = doSuspend(ioScope, onDo)

    protected fun doInit(init: () -> Unit) {
        if (!inited) {
            inited = true
            init()
        }
    }

    protected fun setModel(onUpdate: MODEL.() -> MODEL) = runBlocking {
        mutex.withLock {
            val model = modelFlow.value
            val newModel = onUpdate(model)
            if (model != newModel) {
                modelFlow.value = newModel
            }
        }
    }

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

        jobs.forEach(Job::cancel)
    }
}