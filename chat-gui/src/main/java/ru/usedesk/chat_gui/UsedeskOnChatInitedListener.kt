package ru.usedesk.chat_gui

import ru.usedesk.chat_sdk.domain.UsedeskChat

interface UsedeskOnChatInitedListener {
    fun onChatInited(usedeskChat: UsedeskChat)
}

@Deprecated(
    message = "Use ru.usedesk.chat_gui.UsedeskOnChatInitedListener",
    replaceWith = ReplaceWith(
        "UsedeskOnChatInitedListener",
        "ru.usedesk.chat_gui.UsedeskOnChatInitedListener"
    )
)
typealias IUsedeskOnChatInitedListener = UsedeskOnChatInitedListener
