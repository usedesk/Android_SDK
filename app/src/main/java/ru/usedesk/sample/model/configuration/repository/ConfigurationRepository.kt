package ru.usedesk.sample.model.configuration.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import io.reactivex.*
import ru.usedesk.sample.model.configuration.entity.Configuration

class ConfigurationRepository(private val sharedPreferences: SharedPreferences,
                              private val workScheduler: Scheduler) {
    private var configuration: Configuration? = null

    fun getConfiguration(): Single<Configuration> {
        return Single.create { emitter: SingleEmitter<Configuration> ->
            val configuration = configuration ?: try {
                val json = sharedPreferences.getString(KEY_DATA, "")
                Gson().fromJson(json, Configuration::class.java)
            } catch (e: Exception) {
                null
            } ?: Configuration()

            this.configuration = configuration
            emitter.onSuccess(configuration)
        }.subscribeOn(workScheduler)
    }

    fun setConfiguration(configuration: Configuration): Completable {
        this.configuration = configuration

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