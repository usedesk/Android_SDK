package ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.JsonObject
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_sdk.api.UsedeskApiRepository.Companion.valueOrNull
import javax.inject.Inject

internal class ConfigurationLoader @Inject constructor(
    private val initConfiguration: UsedeskChatConfiguration,
    context: Context,
) : ConfigurationsLoader {

    private val configurationMap: MutableMap<String, UsedeskChatConfiguration> by lazy {
        loadData().toMutableMap()
    }

    private val gson = Gson()
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_MULTI_PROCESS
    )

    private var inited = false
    private var legacyClientToken: String? = null

    override fun initLegacyData(onGetClientToken: () -> String?) {
        if (!inited) {
            inited = true

            legacyClientToken = onGetClientToken()
        }
    }

    override fun getConfig(): UsedeskChatConfiguration {
        return configurationMap[initConfiguration.clientId] ?: initConfiguration
    }

    override fun setConfig(configuration: UsedeskChatConfiguration?) {
        when (configuration) {
            null -> configurationMap.remove(initConfiguration.clientId)
            else -> configurationMap[initConfiguration.clientId] = configuration
        }
        saveData(configurationMap.values.toTypedArray())
    }

    private fun loadData(): Map<String, UsedeskChatConfiguration> {
        return valueOrNull {
            val version = sharedPreferences.getInt(KEY_VERSION, 1)
            if (version < CURRENT_VERSION) {
                loadLegacy(version)
                    ?.let { arrayOf(it) }
                    .also(::saveData)
            } else {
                val json = sharedPreferences.getString(KEY_DATA, null)
                gson.fromJson(json, Array<UsedeskChatConfiguration>::class.java)
            }
        }?.associateBy(UsedeskChatConfiguration::clientId) ?: emptyMap()
    }

    private fun saveData(configurations: Array<UsedeskChatConfiguration>?) {
        val json = gson.toJson(configurations)

        sharedPreferences.edit {
            putString(KEY_DATA, json)
                .putInt(KEY_VERSION, CURRENT_VERSION)
        }
    }

    override fun clearData() {
        sharedPreferences.edit {
            putInt(KEY_VERSION, CURRENT_VERSION)
                .remove(KEY_DATA)
        }
    }

    private fun loadLegacy(oldVersion: Int): UsedeskChatConfiguration? = valueOrNull {
        if (oldVersion < 5) {
            return@valueOrNull null
        }
        val jsonRaw = sharedPreferences.getString(KEY_DATA, null)
        val json = gson.fromJson(jsonRaw, JsonObject::class.java)
        if (oldVersion < 6) {
            if (json.has(KEY_URL_OFFLINE_FORM)) {
                val urlOfflineForm = json.get(KEY_URL_OFFLINE_FORM)
                json.remove(KEY_URL_OFFLINE_FORM)
                json.add(KEY_URL_CHAT_API, urlOfflineForm)
            }
        }
        if (oldVersion < 7) {
            val tempConfig = gson.fromJson(json, UsedeskChatConfiguration::class.java)
            json.addProperty(KEY_CLIENT_ID, tempConfig.oldUserKey())
        }
        return@valueOrNull gson.fromJson(json, UsedeskChatConfiguration::class.java)
    }

    private fun UsedeskChatConfiguration.oldUserKey() =
        "${companyId}_${channelId}_${clientEmail}_${clientPhoneNumber}_${clientName}"

    companion object {
        private const val CURRENT_VERSION = 7

        private const val PREF_NAME = "usedeskSdkConfiguration"

        private const val KEY_VERSION = "versionKey"
        private const val KEY_DATA = "dataKey"

        // Legacy fields
        private const val KEY_URL_CHAT_API = "urlChatApi"
        private const val KEY_URL_OFFLINE_FORM = "urlOfflineForm"
        private const val KEY_CLIENT_ID = "clientId"
    }
}