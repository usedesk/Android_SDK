package ru.usedesk.sample.model.configuration.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
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
                val gson = Gson()
                val jsonRaw = sharedPreferences.getString(KEY_DATA, "")
                val json = gson.fromJson(jsonRaw, JsonObject::class.java)
                if (!json.has("additionalFields")) {
                    json.add("additionalFields", JsonObject())
                }
                if (!json.has("additionalNestedFields")) {
                    json.add("additionalNestedFields", JsonArray())
                }
                gson.fromJson(json, Configuration::class.java)
            } catch (e: Exception) {
                null
            } ?: Configuration()

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