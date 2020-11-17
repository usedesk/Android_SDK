package ru.usedesk.chat_gui.internal._extra.utils

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

private val IO_SCHEDULER = Schedulers.io()

fun <T> observableThis(observableOnSubscribe: ObservableOnSubscribe<T>): Observable<T> {
    return Observable.create(observableOnSubscribe)
}

fun <T> observableIo(observableOnSubscribe: ObservableOnSubscribe<T>): Observable<T> {
    return observableIo(observableThis(observableOnSubscribe))
}

fun <T> singleIo(lambda: () -> T): Single<T> {
    return singleIo(singleThis(lambda))
}

fun <T> singleThis(lambda: () -> T): Single<T> {
    return Single.create { emitter -> emitter.onSuccess(lambda()) }
}

fun completableThis(lambda: () -> Unit): Completable {
    return Completable.create { emitter ->
        lambda()
        emitter.onComplete()
    }
}

fun completableIo(lambda: () -> Unit): Completable {
    return completableIo(completableThis(lambda))
}

fun completableIo(completable: Completable): Completable {
    return completable
            .subscribeOn(IO_SCHEDULER)
            .observeOn(IO_SCHEDULER)
}

fun <T> singleIo(single: Single<T>): Single<T> {
    return single
            .subscribeOn(IO_SCHEDULER)
            .observeOn(IO_SCHEDULER)
}

fun <T> observableIo(observable: Observable<T>): Observable<T> {
    return observable
            .subscribeOn(IO_SCHEDULER)
            .observeOn(IO_SCHEDULER)
}

fun <IN, OUT> mapIo(observable: Observable<IN>, mapper: (IN) -> OUT): Observable<OUT> {
    return observable
            .subscribeOn(IO_SCHEDULER)
            .observeOn(IO_SCHEDULER)
            .map {
                mapper(it)
            }
}

fun success() {}

fun throwable(throwable: Throwable) {
    throwable.printStackTrace()
}

fun dispose(disposable: Disposable?) {
    if (disposable != null && !disposable.isDisposed) {
        disposable.dispose()
    }
}