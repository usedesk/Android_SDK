package ru.usedesk.chat_gui

import ru.usedesk.chat_gui.chat.IUsedeskMediaPlayerAdapter

interface IUsedeskMediaPlayerAdapterKeeper {
    fun getMediaPlayerAdapter(): IUsedeskMediaPlayerAdapter
}