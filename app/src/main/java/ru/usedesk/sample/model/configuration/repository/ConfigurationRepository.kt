package ru.usedesk.sample.model.configuration.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import io.reactivex.*
import ru.usedesk.sample.model.configuration.entity.Configuration

class ConfigurationRepository(private val sharedPreferences: SharedPreferences,
                              private val workScheduler: Scheduler) {
    //TODO: Установите свои значения по умолчанию
    private val defaultModel = Configuration("https://pubsub.usedesk.ru:1992",
            "https://secure.usedesk.ru/",
            "https://secure.usedesk.ru/uapi/v1/",
            "https://api.usedesk.ru/",
            "153712",
            "4",
            "11eb3f39dec94ecf0fe4a80349903e6ad5ce6d75",
            "",
            "android_sdk@usedesk.ru",
            "Иван Иванов",
            "",
            88005553535,
            777,
            "",
            "",
            isForegroundService = false,
            isWithKnowledgeBase = true)

    private var configuration: Configuration? = null

    fun getConfiguration(): Single<Configuration?> {
        return Single.create { emitter: SingleEmitter<Configuration> ->
            val configuration = configuration ?: try {
                val json = sharedPreferences.getString(KEY_DATA, "")
                Gson().fromJson(json, Configuration::class.java)
            } catch (e: Exception) {
                null
            } ?: defaultModel

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