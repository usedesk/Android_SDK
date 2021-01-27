package ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration

import android.content.Context
import android.content.SharedPreferences
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

    private fun getString(value: Long?): String? {
        return value?.toString()
    }

    override fun loadData(): UsedeskChatConfiguration? {
        val urlChat = sharedPreferences.getString(KEY_URL_CHAT, null)
        val urlOfflineForm = sharedPreferences.getString(KEY_URL_OFFLINE_FORM, null)
        val urlToSendFile = sharedPreferences.getString(KEY_URL_TO_SEND_FILE, null)
        val clientId = sharedPreferences.getString(KEY_ID, null)
        val clientEmail = sharedPreferences.getString(KEY_EMAIL, null)
        val clientInitMessage = sharedPreferences.getString(KEY_CLIENT_INIT_MESSAGE, null)
        if (urlChat == null
                || urlOfflineForm == null
                || urlToSendFile == null
                || clientId == null
                || clientEmail == null) {
            return null
        }
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
                urlToSendFile,
                clientId,
                clientEmail,
                clientName,
                clientPhone?.toLongOrNull(),
                clientAdditionalId?.toLongOrNull(),
                clientInitMessage)
    }

    override fun saveData(configuration: UsedeskChatConfiguration) {
        sharedPreferences.edit()
                .putString(KEY_ID, configuration.companyId)
                .putString(KEY_URL_CHAT, configuration.urlChat)
                .putString(KEY_URL_OFFLINE_FORM, configuration.urlOfflineForm)
                .putString(KEY_EMAIL, configuration.email)
                .putString(KEY_NAME, configuration.clientName)
                .putString(KEY_ADDITIONAL_ID, getString(configuration.clientAdditionalId))
                .putString(KEY_CLIENT_INIT_MESSAGE, configuration.clientInitMessage)
                .putString(KEY_PHONE, getString(configuration.clientPhoneNumber))
                .apply()
    }

    override fun clearData() {
        sharedPreferences.edit()
                .remove(KEY_ID)
                .remove(KEY_URL_CHAT)
                .remove(KEY_URL_OFFLINE_FORM)
                .remove(KEY_EMAIL)
                .apply()
    }

    companion object {
        private const val PREF_NAME = "usedeskSdkConfiguration"
        private const val KEY_ID = "id"
        private const val KEY_URL_CHAT = "url"
        private const val KEY_URL_OFFLINE_FORM = "offlineUrl"
        private const val KEY_URL_TO_SEND_FILE = "urlToSendFile"
        private const val KEY_EMAIL = "email"
        private const val KEY_NAME = "name"
        private const val KEY_PHONE = "phone"
        private const val KEY_ADDITIONAL_ID = "additionalId"
        private const val KEY_CLIENT_INIT_MESSAGE = "clientInitMessage"
    }
}