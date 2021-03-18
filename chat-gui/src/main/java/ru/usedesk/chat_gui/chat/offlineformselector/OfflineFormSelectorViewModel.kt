package ru.usedesk.chat_gui.chat.offlineformselector

import androidx.lifecycle.MutableLiveData
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.common_gui.UsedeskViewModel

internal class OfflineFormSelectorViewModel : UsedeskViewModel() {

    private val usedeskChat = UsedeskChatSdk.requireInstance()

    val configuration = UsedeskChatSdk.requireConfiguration()

    val selectedIndexLiveData = MutableLiveData<Int>()

    fun onSelected(selectedIndex: Int) {
        selectedIndexLiveData.value = selectedIndex
    }
}