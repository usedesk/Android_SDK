package ru.usedesk.chat_gui.chat.messages.adapters.holders

import android.text.Html
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item.ItemList
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Field

internal class ItemListViewHolder(
    private val binding: MessageFormsAdapter.ItemListBinding,
    private val onEvent: (Event) -> Unit
) : BaseViewHolder(binding.rootView) {

    override fun bind(
        messageId: Long,
        item: Item,
        scope: CoroutineScope,
        stateFlow: StateFlow<MessagesViewModel.State>
    ) {
        item as ItemList
        var list: Field.List? = null
        var parentList: Field.List? = null
        var formState: UsedeskForm.State? = null
        stateFlow.onEach { state ->
            val form = state.formMap[messageId]
            if (form != null) {
                val newList = form.fields.first { it.id == item.fieldId } as Field.List
                val newParentList =
                    form.fields.firstOrNull { it.id == newList.parentId } as? Field.List
                val newFormState = form.state
                if (list != newList || parentList != newParentList || formState != newFormState) {
                    list = newList
                    formState = newFormState
                    parentList = newParentList
                    update(
                        messageId,
                        newList,
                        newParentList,
                        newFormState
                    )
                }
            }
        }.launchIn(viewHolderScope)
    }

    private fun update(
        messageId: Long,
        list: Field.List,
        parentList: Field.List?,
        formState: UsedeskForm.State
    ) {
        binding.tvText.apply {
            when (val selected = list.selected) {
                null -> {
                    text = Html.fromHtml(
                        list.name + when {
                            list.required -> REQUIRED_POSTFIX_HTML
                            else -> ""
                        }
                    )
                    setTextColor(binding.rootView.resources.getColor(R.color.usedesk_gray_2)) //TODO: styleValues
                }
                else -> {
                    text = selected.name
                    setTextColor(binding.rootView.resources.getColor(R.color.usedesk_black_3)) //TODO: styleValues
                }
            }
        }
        binding.lFrame.setBackgroundResource(
            when {
                list.hasError -> R.drawable.usedesk_message_field_error
                else -> R.drawable.usedesk_message_field_simple
            }
        )
        val enabled = (parentList == null || parentList.selected != null)
                && formState == UsedeskForm.State.LOADED
        binding.lClickable.setOnClickListener {
            onEvent(Event.FormListClicked(messageId, list))
        }
        binding.lClickable.isClickable = enabled
        binding.lClickable.isFocusable = enabled
    }
}