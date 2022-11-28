package ru.usedesk.knowledgebase_sdk

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

internal object RxUtil {
    private fun <T : Any> safeSingle(run: () -> T): Single<T> = Single.create { emitter ->
        try {
            emitter.onSuccess(run())
        } catch (e: Exception) {
            if (!emitter.isDisposed) {
                emitter.onError(e)
            }
        }
    }

    fun <T : Any> safeSingleIo(ioScheduler: Scheduler, run: () -> T): Single<T> = safeSingle(run)
        .subscribeOn(ioScheduler)
        .observeOn(AndroidSchedulers.mainThread())

    private fun safeCompletable(run: () -> Unit): Completable = Completable.create { emitter ->
        try {
            run()
        } catch (e: Exception) {
            if (!emitter.isDisposed) {
                emitter.onError(e)
            }
        }
        emitter.onComplete()
    }

    fun safeCompletableIo(ioScheduler: Scheduler, run: () -> Unit) = safeCompletable(run)
        .subscribeOn(ioScheduler)
        .observeOn(AndroidSchedulers.mainThread())
}