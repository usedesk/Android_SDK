package ru.usedesk.chat_gui.chat.messages.adapters.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter
import ru.usedesk.chat_sdk.entity.UsedeskForm

internal sealed class BaseViewHolder(rootView: View) :
    RecyclerView.ViewHolder(rootView) {

    abstract fun bind(
        messageId: Long,
        item: MessageFormsAdapter.Item,
        state: UsedeskForm.State
    )

    companion object {
        const val REQUIRED_POSTFIX_HTML = "&thinsp;<font color=#ff0000>*</font>"
    }
}