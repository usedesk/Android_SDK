package ru.usedesk.chat_gui.chat.messages.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.FormItemState
import ru.usedesk.chat_gui.chat.messages.adapters.holders.*
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Form
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem

//TODO: вытащить вьюхолдеры во вне
internal class MessageItemsAdapter(
    recyclerView: RecyclerView,
    private val onEvent: (Event) -> Unit,
    private val onButtonClick: (Form.Button) -> Unit
) : RecyclerView.Adapter<BaseViewHolder<out Form, out FormItemState>>() {

    private var messageId = 0L
    private var forms: List<Form> = listOf()
    private var itemsState: Map<Long, FormItemState> = mapOf()

    init {
        recyclerView.adapter = this
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(
        messageAgentText: UsedeskMessageAgentText,
        agentItems: Map<Long, FormItemState>
    ) {
        val oldItems = forms
        val oldAgentItems = itemsState
        forms = messageAgentText.forms
        when (messageAgentText.id) {
            this.messageId -> DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = oldItems.size

                override fun getNewListSize() = forms.size

                override fun areItemsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean = oldItems[oldItemPosition].id == forms[newItemPosition].id

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    val oldItem = oldItems[oldItemPosition]
                    val newItem = forms[newItemPosition]
                    return oldItem.name == newItem.name && when (oldItem) {
                        is Form.Button -> oldItem.areContentsTheSame(
                            newItem as Form.Button,
                            oldAgentItems[oldItem.id] as FormItemState.Button?,
                            agentItems[oldItem.id] as FormItemState.Button?
                        )
                        is Form.Field.CheckBox -> oldItem.areContentsTheSame(
                            newItem as Form.Field.CheckBox,
                            oldAgentItems[oldItem.id] as FormItemState.CheckBox?,
                            agentItems[oldItem.id] as FormItemState.CheckBox?
                        )
                        is Form.Field.List -> oldItem.areContentsTheSame(
                            newItem as Form.Field.List,
                            oldAgentItems[oldItem.id] as FormItemState.ItemList?,
                            agentItems[oldItem.id] as FormItemState.ItemList?
                        )
                        is Form.Field.Text -> oldItem.areContentsTheSame(
                            newItem as Form.Field.Text,
                            oldAgentItems[oldItem.id] as FormItemState.Text?,
                            agentItems[oldItem.id] as FormItemState.Text?
                        )
                    }
                }
            }).dispatchUpdatesTo(this)
            else -> {
                this.messageId = messageAgentText.id
                notifyDataSetChanged()
            }
        }
    }

    private fun Form.Field.List.areContentsTheSame(
        newForm: Form.Field.List,
        oldFormItemState: FormItemState.ItemList?,
        newFormItemState: FormItemState.ItemList?
    ): Boolean = items == newForm.items ||
            oldFormItemState?.selected == newFormItemState?.selected

    private fun Form.Field.CheckBox.areContentsTheSame(
        newForm: Form.Field.CheckBox,
        oldFormItemState: FormItemState.CheckBox?,
        newFormItemState: FormItemState.CheckBox?
    ): Boolean = oldFormItemState?.checked == newFormItemState?.checked

    private fun Form.Field.Text.areContentsTheSame(
        newForm: Form.Field.Text,
        oldFormItemState: FormItemState.Text?,
        newFormItemState: FormItemState.Text?
    ): Boolean = true

    private fun Form.Button.areContentsTheSame(
        newForm: Form.Button,
        oldFormItemState: FormItemState.Button?,
        newFormItemState: FormItemState.Button?
    ): Boolean = oldFormItemState?.enabled == newFormItemState?.enabled

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.usedesk_chat_message_item_button -> ButtonViewHolder(
            inflateItem(
                parent,
                R.layout.usedesk_chat_message_item_button,
                R.style.Usedesk_Chat_Message_Text_Button,
                ::ButtonBinding
            ),
            onEvent,
            onButtonClick
        )
        R.layout.usedesk_chat_message_item_text -> TextViewHolder(
            inflateItem(
                parent,
                R.layout.usedesk_chat_message_item_text,
                R.style.Usedesk_Chat_Message_Text_Button,
                ::TextBinding
            ),
            onEvent
        )
        R.layout.usedesk_chat_message_item_checkbox -> CheckBoxViewHolder(
            inflateItem(
                parent,
                R.layout.usedesk_chat_message_item_checkbox,
                R.style.Usedesk_Chat_Message_Text_Button,
                ::CheckBoxBinding
            ),
            onEvent
        )
        R.layout.usedesk_chat_message_item_itemlist -> ItemListViewHolder(
            inflateItem(
                parent,
                R.layout.usedesk_chat_message_item_itemlist,
                R.style.Usedesk_Chat_Message_Text_ItemList,
                ::ItemListBinding
            ),
            onEvent
        )
        else -> throw RuntimeException("Unknown view type: $viewType")
    }

    override fun getItemViewType(position: Int) = when (forms[position]) {
        is Form.Button -> R.layout.usedesk_chat_message_item_button
        is Form.Field.Text -> R.layout.usedesk_chat_message_item_text
        is Form.Field.CheckBox -> R.layout.usedesk_chat_message_item_checkbox
        is Form.Field.List -> R.layout.usedesk_chat_message_item_itemlist
        else -> 0
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<out Form, out FormItemState>,
        position: Int
    ) {
        val formItem = forms[position]
        val state = itemsState[formItem.id]
        holder.bindItem(
            messageId,
            formItem,
            state ?: when (formItem) {
                is Form.Button -> FormItemState.Button(
                    enabled = formItem.id != Form.Button.FORM_APPLY_BUTTON_ID
                )
                is Form.Field.CheckBox -> FormItemState.CheckBox()
                is Form.Field.List -> FormItemState.ItemList()
                is Form.Field.Text -> FormItemState.Text()
            }
        )
    }

    override fun getItemCount() = forms.size

    internal class ButtonBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
    }

    internal class TextBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val etText: EditText = rootView.findViewById(R.id.et_text)
    }

    internal class CheckBoxBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvText: TextView = rootView.findViewById(R.id.tv_text)
    }

    internal class ItemListBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvText: TextView = rootView.findViewById(R.id.tv_text)
    }
}