package ru.usedesk.chat_gui.chat.messages.adapters.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.FormState
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Form

internal sealed class BaseViewHolder<ITEM : Form, STATE : FormState>(rootView: View) :
    RecyclerView.ViewHolder(rootView) {

    @Suppress("UNCHECKED_CAST")
    fun bindItem(messageId: Long, form: Form, state: FormState) {
        bind(messageId, form as ITEM, state as STATE)
    }

    protected abstract fun bind(messageId: Long, form: ITEM, formState: STATE)
}