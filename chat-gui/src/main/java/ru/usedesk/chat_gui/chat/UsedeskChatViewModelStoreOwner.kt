package ru.usedesk.chat_gui.chat

import androidx.lifecycle.ViewModelStoreOwner
import ru.usedesk.common_gui.UsedeskFragment

interface UsedeskChatViewModelStoreOwner : ViewModelStoreOwner

internal fun UsedeskFragment.requireChatViewModelStoreOwner(): ViewModelStoreOwner {
    return findChatViewModelStoreOwner() ?: throw RuntimeException("Can't find ViewModelStoreOwner")
}

internal fun UsedeskFragment.findChatViewModelStoreOwner(): ViewModelStoreOwner? {
    return findParent<UsedeskChatViewModelStoreOwner>()
        ?: findParent<UsedeskChatScreen>()
}