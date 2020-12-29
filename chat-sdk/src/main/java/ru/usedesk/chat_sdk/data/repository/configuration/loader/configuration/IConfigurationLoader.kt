package ru.usedesk.chat_sdk.data.repository.configuration.loader.configuration

import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration

interface IConfigurationLoader {
    fun loadData(): UsedeskChatConfiguration?

    fun getData(): UsedeskChatConfiguration

    fun setData(data: UsedeskChatConfiguration?)

    fun clearData()
}