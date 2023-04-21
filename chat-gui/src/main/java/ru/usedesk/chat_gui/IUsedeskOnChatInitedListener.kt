
package ru.usedesk.chat_gui

import ru.usedesk.chat_sdk.domain.IUsedeskChat

interface IUsedeskOnChatInitedListener {
    fun onChatInited(usedeskChat: IUsedeskChat)
}