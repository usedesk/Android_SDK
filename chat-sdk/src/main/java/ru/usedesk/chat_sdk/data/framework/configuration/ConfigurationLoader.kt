package ru.usedesk.chat_sdk.data.framework.configuration

import android.content.Context
import android.content.SharedPreferences
import ru.usedesk.chat_sdk.data.framework.info.DataLoader
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import toothpick.InjectConstructor

@InjectConstructor
class ConfigurationLoader(
        context: Context
) : DataLoader<UsedeskChatConfiguration>(), IConfigurationLoader {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
    )

    private fun getString(value: Long?): String? {
        return value?.toString()
    }

    override fun loadData(): UsedeskChatConfiguration? {
        val id = sharedPreferences.getString(KEY_ID, null)
        val url = sharedPreferences.getString(KEY_URL, null)
        val offlineUrl = sharedPreferences.getString(KEY_OFFLINE_URL, null)
        val email = sharedPreferences.getString(KEY_EMAIL, null)
        val initClientMessage = sharedPreferences.getString(KEY_CLIENT_INIT_MESSAGE, null)
        if (id == null || url == null || email == null || offlineUrl == null) {
            return null
        }
        var name: String? = null
        var phone: String? = null
        var additionalId: String? = null
        try {
            name = sharedPreferences.getString(KEY_NAME, null)
            phone = sharedPreferences.getString(KEY_PHONE, null)
            additionalId = sharedPreferences.getString(KEY_ADDITIONAL_ID, null)
        } catch (e: ClassCastException) {
            try {
                phone = sharedPreferences.getLong(KEY_PHONE, 0).toString() //Для миграции с версий, где хранился Long
                additionalId = sharedPreferences.getLong(KEY_ADDITIONAL_ID, 0).toString()
            } catch (e1: ClassCastException) {
                e.printStackTrace()
            }
        }
        return UsedeskChatConfiguration(id,
                email,
                url,
                offlineUrl,
                name,
                phone?.toLongOrNull(),
                additionalId?.toLongOrNull(),
                initClientMessage)
    }

    override fun saveData(configuration: UsedeskChatConfiguration) {
        sharedPreferences.edit()
                .putString(KEY_ID, configuration.companyId)
                .putString(KEY_URL, configuration.url)
                .putString(KEY_OFFLINE_URL, configuration.offlineFormUrl)
                .putString(KEY_EMAIL, configuration.email)
                .putString(KEY_NAME, configuration.clientName)
                .putString(KEY_ADDITIONAL_ID, getString(configuration.clientAdditionalId))
                .putString(KEY_CLIENT_INIT_MESSAGE, configuration.initClientMessage)
                .putString(KEY_PHONE, getString(configuration.clientPhoneNumber))
                .apply()
    }

    override fun clearData() {
        super.clearData()
        sharedPreferences.edit()
                .remove(KEY_ID)
                .remove(KEY_URL)
                .remove(KEY_OFFLINE_URL)
                .remove(KEY_EMAIL)
                .apply()
    }

    companion object {
        private const val PREF_NAME = "usedeskSdkConfiguration"
        private const val KEY_ID = "id"
        private const val KEY_URL = "url"
        private const val KEY_OFFLINE_URL = "offlineUrl"
        private const val KEY_EMAIL = "email"
        private const val KEY_NAME = "name"
        private const val KEY_PHONE = "phone"
        private const val KEY_ADDITIONAL_ID = "additionalId"
        private const val KEY_CLIENT_INIT_MESSAGE = "clientInitMessage"
    }
}