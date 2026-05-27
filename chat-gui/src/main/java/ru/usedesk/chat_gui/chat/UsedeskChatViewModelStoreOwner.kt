package ru.usedesk.chat_gui.chat

import androidx.lifecycle.ViewModelStoreOwner
import ru.usedesk.common_gui.UsedeskFragment

interface UsedeskChatViewModelStoreOwner : ViewModelStoreOwner

@Deprecated(
    message = "Use ru.usedesk.chat_gui.chat.UsedeskChatViewModelStoreOwner",
    replaceWith = ReplaceWith(
        "UsedeskChatViewModelStoreOwner",
        "ru.usedesk.chat_gui.chat.UsedeskChatViewModelStoreOwner"
    )
)
typealias IUsedeskChatViewModelStoreOwner = UsedeskChatViewModelStoreOwner

internal fun UsedeskFragment.requireChatViewModelStoreOwner(): ViewModelStoreOwner =
    findChatViewModelStoreOwner() ?: throw RuntimeException("Can't find ViewModelStoreOwner")

internal fun UsedeskFragment.findChatViewModelStoreOwner(): ViewModelStoreOwner? =
    findParent<UsedeskChatViewModelStoreOwner>()
        ?: findParent<UsedeskChatScreen>()
