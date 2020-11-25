package ru.usedesk.chat_sdk.internal.data.framework.configuration

import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration

interface IConfigurationLoader {
    fun loadData(): UsedeskChatConfiguration?

    fun getData(): UsedeskChatConfiguration

    fun setData(data: UsedeskChatConfiguration?)

    fun clearData()
}