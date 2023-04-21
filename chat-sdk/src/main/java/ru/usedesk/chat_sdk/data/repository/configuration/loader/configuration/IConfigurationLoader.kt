
package ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration

import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration

internal interface IConfigurationLoader {
    fun initLegacyData(onGetClientToken: () -> String?)

    fun getData(): Array<UsedeskChatConfiguration>?

    fun setData(data: Array<UsedeskChatConfiguration>?)

    fun clearData()
}