package ru.usedesk.sample.model.configuration.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import ru.usedesk.sample.model.configuration.entity.Configuration

class ConfigurationRepository(
    private val sharedPreferences: SharedPreferences
) {
    private val configurationFlow: MutableStateFlow<Configuration>

    init {
        val configuration = try {
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

        configurationFlow = MutableStateFlow(configuration)
    }

    fun getConfigurationFlow(): StateFlow<Configuration> = configurationFlow

    fun setConfiguration(configuration: Configuration) {
        val json = Gson().toJson(configuration)
        sharedPreferences.edit()
            .putString(KEY_DATA, json)
            .apply()

        runBlocking {
            configurationFlow.emit(configuration)
        }
    }

    companion object {
        private const val KEY_DATA = "sampleConfigurationKey"
    }
}