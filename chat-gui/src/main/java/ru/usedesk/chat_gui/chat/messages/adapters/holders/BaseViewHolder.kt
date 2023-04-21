
package ru.usedesk.chat_gui.chat.messages.adapters.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormAdapter

internal sealed class BaseViewHolder(rootView: View) :
    RecyclerView.ViewHolder(rootView) {
    protected var viewHolderScope = CoroutineScope(Dispatchers.Main)

    open fun bind(
        messageId: Long,
        item: MessageFormAdapter.Item,
        scope: CoroutineScope,
        stateFlow: StateFlow<MessagesViewModel.State>
    ) {
        viewHolderScope.cancel()
        viewHolderScope = CoroutineScope(scope.coroutineContext + Job())
    }

    companion object {
        const val REQUIRED_POSTFIX_HTML = "&thinsp;<font color=#ff0000>*</font>"
    }
}