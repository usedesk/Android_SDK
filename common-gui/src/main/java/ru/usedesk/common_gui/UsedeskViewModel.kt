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
    val mainThread = AndroidSchedulers.mainThread()

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

    protected fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    protected fun doIt(
        completable: Completable,
        onCompleted: () -> Unit = {},
        onThrowable: (Throwable) -> Unit = { throwable(it) }
    ) = completable.observeOn(mainThread)
        .subscribe({ onCompleted() }, { onThrowable(it) }).also {
            addDisposable(it)
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
    ) = observable.observeOn(mainThread)
        .subscribe({ onValue(it) }, { onThrowable(it) }).also {
            addDisposable(it)
        }

    protected fun <T> doIt(
        single: Single<T>,
        onValue: (T) -> Unit = {},
        onThrowable: (Throwable) -> Unit = { throwable(it) }
    ) = doIt(single.toObservable(), onValue, onThrowable)

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

        disposables.forEach(Disposable::dispose)

        jobs.forEach(Job::cancel)
    }
}