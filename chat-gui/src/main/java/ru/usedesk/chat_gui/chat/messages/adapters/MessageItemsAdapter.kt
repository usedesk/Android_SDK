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
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.FormState
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
) : RecyclerView.Adapter<BaseViewHolder<out Form, out FormState>>() {

    private var messageId = 0L
    private var forms: List<Form> = listOf()
    private var itemsState: Map<Long, FormState> = mapOf()

    init {
        recyclerView.adapter = this
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(
        messageAgentText: UsedeskMessageAgentText,
        agentItems: Map<Long, FormState>
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
                            oldAgentItems[oldItem.id] as FormState.Button?,
                            agentItems[oldItem.id] as FormState.Button?
                        )
                        is Form.Field.CheckBox -> oldItem.areContentsTheSame(
                            newItem as Form.Field.CheckBox,
                            oldAgentItems[oldItem.id] as FormState.CheckBox?,
                            agentItems[oldItem.id] as FormState.CheckBox?
                        )
                        is Form.Field.List -> oldItem.areContentsTheSame(
                            newItem as Form.Field.List,
                            oldAgentItems[oldItem.id] as FormState.List?,
                            agentItems[oldItem.id] as FormState.List?
                        )
                        is Form.Field.Text -> oldItem.areContentsTheSame(
                            newItem as Form.Field.Text,
                            oldAgentItems[oldItem.id] as FormState.Text?,
                            agentItems[oldItem.id] as FormState.Text?
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
        oldFormState: FormState.List?,
        newFormState: FormState.List?
    ): Boolean = items == newForm.items ||
            oldFormState?.selected == newFormState?.selected

    private fun Form.Field.CheckBox.areContentsTheSame(
        newForm: Form.Field.CheckBox,
        oldFormState: FormState.CheckBox?,
        newFormState: FormState.CheckBox?
    ): Boolean = oldFormState?.checked == newFormState?.checked

    private fun Form.Field.Text.areContentsTheSame(
        newForm: Form.Field.Text,
        oldFormState: FormState.Text?,
        newFormState: FormState.Text?
    ): Boolean = true

    private fun Form.Button.areContentsTheSame(
        newForm: Form.Button,
        oldFormState: FormState.Button?,
        newFormState: FormState.Button?
    ): Boolean = oldFormState?.enabled == newFormState?.enabled

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
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<out Form, out FormState>,
        position: Int
    ) {
        val formItem = forms[position]
        val state = itemsState[formItem.id]
        holder.bindItem(
            messageId,
            formItem,
            state ?: when (formItem) {
                is Form.Button -> FormState.Button(
                    enabled = formItem.id != Form.Button.FORM_APPLY_BUTTON_ID
                )
                is Form.Field.CheckBox -> FormState.CheckBox()
                is Form.Field.List -> FormState.List()
                is Form.Field.Text -> FormState.Text()
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