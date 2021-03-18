package ru.usedesk.chat_gui.chat.offlineform

internal interface IOnOfflineFormSelectorClick {
    fun onOfflineFormSelectorClick(items: List<String>, selectedIndex: Int)
}