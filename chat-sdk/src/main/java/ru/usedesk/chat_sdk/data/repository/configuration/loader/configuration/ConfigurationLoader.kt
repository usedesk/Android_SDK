package ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import ru.usedesk.chat_sdk.data.repository._extra.DataLoader
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import toothpick.InjectConstructor

@InjectConstructor
internal class ConfigurationLoader(
        context: Context
) : DataLoader<UsedeskChatConfiguration>(), IConfigurationLoader {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_MULTI_PROCESS
    )

    private val gson: Gson = Gson()

    override fun loadData(): UsedeskChatConfiguration? {
        val version = sharedPreferences.getInt(KEY_VERSION, 1)
        if (version < CURRENT_VERSION) {
            migrate(version)
        }

        return try {
            val json = sharedPreferences.getString(KEY_DATA, null)
            gson.fromJson(json, UsedeskChatConfiguration::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun migrate(oldVersion: Int) {
        val configuration = loadLegacy(oldVersion)
        if (configuration != null) {
            saveData(configuration)
        } else {
            clearData()
        }
    }

    override fun saveData(data: UsedeskChatConfiguration) {
        val json = gson.toJson(data)

        sharedPreferences.edit()
                .putString(KEY_DATA, json)
                .putInt(KEY_VERSION, CURRENT_VERSION)
                .apply()
    }

    override fun clearData() {
        sharedPreferences.edit()
                .putInt(KEY_VERSION, CURRENT_VERSION)
                .remove(KEY_DATA)
                .apply()
    }

    private fun loadLegacy(oldVersion: Int): UsedeskChatConfiguration? {
        if (oldVersion == 1) {
            val urlChat = sharedPreferences.getString(KEY_URL_CHAT, null)
            val urlOfflineForm = sharedPreferences.getString(KEY_URL_OFFLINE_FORM, null)
            val companyId = sharedPreferences.getString(KEY_ID, null)
            val clientEmail = sharedPreferences.getString(KEY_EMAIL, null)
            val clientInitMessage = sharedPreferences.getString(KEY_CLIENT_INIT_MESSAGE, null)
            if (urlChat != null
                    && urlOfflineForm != null
                    && companyId != null) {
                var clientName: String? = null
                var clientPhone: String? = null
                var clientAdditionalId: String? = null
                try {
                    clientName = sharedPreferences.getString(KEY_NAME, null)
                    clientPhone = sharedPreferences.getString(KEY_PHONE, null)
                    clientAdditionalId = sharedPreferences.getString(KEY_ADDITIONAL_ID, null)
                } catch (e: ClassCastException) {
                    try {
                        clientPhone = sharedPreferences.getLong(KEY_PHONE, 0).toString() //Для миграции с версий, где хранился Long
                        clientAdditionalId = sharedPreferences.getLong(KEY_ADDITIONAL_ID, 0).toString()
                    } catch (e1: ClassCastException) {
                        e.printStackTrace()
                    }
                }
                return UsedeskChatConfiguration(
                        urlChat,
                        urlOfflineForm,
                        "https://secure.usedesk.ru/uapi/v1/send_file",
                        companyId,
                        null,
                        clientEmail,
                        clientName,
                        null,
                        clientPhone?.toLongOrNull(),
                        clientAdditionalId?.toLongOrNull(),
                        clientInitMessage)
            }
        }

        return null
    }

    companion object {
        private const val CURRENT_VERSION = 2

        private const val PREF_NAME = "usedeskSdkConfiguration"

        private const val KEY_VERSION = "versionKey"
        private const val KEY_DATA = "dataKey"

        //Legacy:
        private const val KEY_ID = "id"
        private const val KEY_URL_CHAT = "url"
        private const val KEY_URL_OFFLINE_FORM = "offlineUrl"
        private const val KEY_EMAIL = "email"
        private const val KEY_NAME = "name"
        private const val KEY_PHONE = "phone"
        private const val KEY_ADDITIONAL_ID = "additionalId"
        private const val KEY_CLIENT_INIT_MESSAGE = "clientInitMessage"
    }
}