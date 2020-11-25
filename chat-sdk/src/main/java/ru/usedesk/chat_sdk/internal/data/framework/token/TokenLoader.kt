package ru.usedesk.chat_sdk.internal.data.framework.token

import android.content.Context
import android.content.SharedPreferences
import ru.usedesk.chat_sdk.internal.data.framework.info.DataLoader
import toothpick.InjectConstructor

@InjectConstructor
class TokenLoader(
        context: Context
) : DataLoader<String>(), ITokenLoader {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
    )

    override fun loadData(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    override fun saveData(data: String) {
        sharedPreferences.edit()
                .putString(KEY_TOKEN, data)
                .apply()
    }

    override fun clearData() {
        super.clearData()
        sharedPreferences.edit()
                .remove(KEY_TOKEN)
                .apply()
    }

    companion object {
        private const val PREF_NAME = "usedeskSdkToken"
        private const val KEY_TOKEN = "token"
    }
}