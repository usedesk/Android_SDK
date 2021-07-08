package ru.usedesk.sample.model.configuration.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import io.reactivex.*
import io.reactivex.subjects.BehaviorSubject
import ru.usedesk.sample.model.configuration.entity.Configuration

class ConfigurationRepository(
    private val sharedPreferences: SharedPreferences,
    private val workScheduler: Scheduler
) {
    private var configuration: Configuration? = null
    private val configurationSubject = BehaviorSubject.create<Configuration>()

    fun getConfiguration(): Single<Configuration> {
        return Single.create { emitter: SingleEmitter<Configuration> ->
            val configuration = configuration ?: try {
                val json = sharedPreferences.getString(KEY_DATA, "")
                Gson().fromJson(json, Configuration::class.java)
            } catch (e: Exception) {
                null
            } ?: Configuration.default

            configurationSubject.onNext(configuration)
            this.configuration = configuration
            emitter.onSuccess(configuration)
        }.subscribeOn(workScheduler)
    }

    fun getConfigurationObservable(): Observable<Configuration> = configurationSubject

    fun setConfiguration(configuration: Configuration): Completable {
        this.configuration = configuration
        configurationSubject.onNext(configuration)

        return Completable.create { emitter: CompletableEmitter ->
            val json = Gson().toJson(configuration)
            sharedPreferences.edit()
                .putString(KEY_DATA, json)
                .apply()
            emitter.onComplete()
        }.subscribeOn(workScheduler)
    }

    companion object {
        private const val KEY_DATA = "sampleConfigurationKey"
    }
}