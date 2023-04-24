
package ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import ru.usedesk.chat_sdk.data.repository._extra.DataLoader
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import javax.inject.Inject

internal class ConfigurationLoader @Inject constructor(
    context: Context,
) : DataLoader<Array<UsedeskChatConfiguration>>(), IConfigurationLoader {

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

    override fun loadData(): Array<UsedeskChatConfiguration>? {
        val version = sharedPreferences.getInt(KEY_VERSION, 1)
        if (version < CURRENT_VERSION) {
            migrate(version)
        }

        return try {
            val json = sharedPreferences.getString(KEY_DATA, null)
            gson.fromJson(json, Array<UsedeskChatConfiguration>::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun migrate(oldVersion: Int) {
        setData(loadLegacy(oldVersion))
    }

    override fun saveData(data: Array<UsedeskChatConfiguration>) {
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

    private fun loadLegacy(oldVersion: Int): Array<UsedeskChatConfiguration>? = when (oldVersion) {
        1 -> {
            val urlChat = sharedPreferences.getString(KEY_URL_CHAT, null)
            val urlChatApi = sharedPreferences.getString(KEY_URL_OFFLINE_FORM, null)
            val companyId = sharedPreferences.getString(KEY_ID, null)
            val clientEmail = sharedPreferences.getString(KEY_EMAIL, null)
            val clientInitMessage = sharedPreferences.getString(KEY_CLIENT_INIT_MESSAGE, null)
            when {
                urlChat != null
                        && urlChatApi != null
                        && companyId != null -> {
                    var clientName: String? = null
                    var clientPhone: String? = null
                    var clientAdditionalId: String? = null
                    try {
                        clientName = sharedPreferences.getString(KEY_NAME, null)
                        clientPhone = sharedPreferences.getString(KEY_PHONE, null)
                        clientAdditionalId =
                            sharedPreferences.getString(KEY_ADDITIONAL_ID, null)
                    } catch (e: ClassCastException) {
                        try {
                            clientPhone = sharedPreferences.getLong(KEY_PHONE, 0)
                                .toString() //For migrations with long
                            clientAdditionalId =
                                sharedPreferences.getLong(KEY_ADDITIONAL_ID, 0).toString()
                        } catch (e1: ClassCastException) {
                            e.printStackTrace()
                        }
                    }
                    arrayOf(
                        UsedeskChatConfiguration(
                            urlChat = urlChat,
                            urlChatApi = urlChatApi,
                            companyId = companyId,
                            channelId = "",
                            clientToken = legacyClientToken,
                            clientEmail = clientEmail,
                            clientName = clientName,
                            clientNote = null,
                            clientPhoneNumber = clientPhone?.toLongOrNull(),
                            clientAdditionalId = clientAdditionalId,
                            clientInitMessage = clientInitMessage
                        )
                    )
                }
                else -> null
            }
        }
        2, 3, 4 -> try {
            val jsonRaw = sharedPreferences.getString(KEY_DATA, null)
            val json = gson.fromJson(jsonRaw, JsonObject::class.java)
            if (!json.has("channelId")) {
                json.addProperty("channelId", "")
            }
            if (!json.has("clientToken")) {
                json.addProperty("clientToken", legacyClientToken)
            }
            if (!json.has("cacheMessagesWithFile")) {
                json.addProperty("cacheMessagesWithFile", true)
            }
            if (!json.has("additionalFields")) {
                json.add("additionalFields", JsonObject())
            }
            if (!json.has("additionalNestedFields")) {
                json.add("additionalNestedFields", JsonArray())
            }
            val configuration = gson.fromJson(json, UsedeskChatConfiguration::class.java)
            arrayOf(configuration)
        } catch (e: Exception) {
            null
        }
        else -> null
    }

    companion object {
        private const val CURRENT_VERSION = 5

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