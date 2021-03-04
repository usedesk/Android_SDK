package ru.usedesk.common_sdk.utils

import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object UsedeskRxUtil {

    fun <T : Any> safeSingle(run: () -> T): Single<T> {
        return Single.create { emitter ->
            try {
                emitter.onSuccess(run())
            } catch (e: Exception) {
                if (!emitter.isDisposed) {
                    emitter.onError(e)
                }
            }
        }
    }

    fun <T : Any> safeSingleIo(run: () -> T): Single<T> {
        return safeSingle(run)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun safeCompletable(run: () -> Unit): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            try {
                run()
            } catch (e: Exception) {
                if (!emitter.isDisposed) {
                    emitter.onError(e)
                }
            }
            emitter.onComplete()
        }
    }

    fun safeCompletableIo(run: () -> Unit): Completable {
        return safeCompletable(run)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}