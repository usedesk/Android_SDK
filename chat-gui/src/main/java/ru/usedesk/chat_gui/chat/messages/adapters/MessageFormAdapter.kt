package ru.usedesk.chat_gui.chat.messages.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormAdapter.Item.ItemButton
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormAdapter.Item.ItemCheckBox
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormAdapter.Item.ItemList
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormAdapter.Item.ItemText
import ru.usedesk.chat_gui.chat.messages.adapters.holders.BaseViewHolder
import ru.usedesk.chat_gui.chat.messages.adapters.holders.ButtonViewHolder
import ru.usedesk.chat_gui.chat.messages.adapters.holders.CheckBoxViewHolder
import ru.usedesk.chat_gui.chat.messages.adapters.holders.ItemListViewHolder
import ru.usedesk.chat_gui.chat.messages.adapters.holders.TextViewHolder
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.chat_sdk.entity.UsedeskForm.Field
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Button
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.chat_gui.R as chatR

internal class MessageFormAdapter(
    private val viewModel: MessagesViewModel,
    private val lifecycleScope: CoroutineScope
) : RecyclerView.Adapter<BaseViewHolder>() {

    private var adapterScope = CoroutineScope(Dispatchers.Main)
    private var messageId: String = ""
    private var items = listOf<Item>()
    private var buttons = listOf<Button>()
    private var form: UsedeskForm? = null

    private fun getItems(
        buttons: List<Button>,
        form: UsedeskForm?
    ) = buttons.map { ItemButton(it) } + when (form) {
        null -> listOf()
        else -> when (form.state) {
            UsedeskForm.State.NOT_LOADED,
            UsedeskForm.State.LOADING_FAILED,
            UsedeskForm.State.LOADING -> listOf()
            else -> form.fields.map { field ->
                when (field) {
                    is Field.CheckBox -> ItemCheckBox(field.id)
                    is Field.List -> ItemList(field.id)
                    is Field.Text -> ItemText(field.id)
                }
            }
        } + listOf(ItemButton(null))
    }

    private fun onUpdate(form: UsedeskForm?) {
        val oldForm = this.form
        this.form = form
        val oldItems = items
        items = getItems(buttons, form)
        when {
            form != null && oldForm?.id == form.id -> if (oldForm.state != form.state) {
                DiffUtil.calculateDiff(
                    object : DiffUtil.Callback() {
                        override fun getOldListSize() = oldItems.size

                        override fun getNewListSize() = items.size

                        override fun areItemsTheSame(
                            oldItemPosition: Int,
                            newItemPosition: Int
                        ): Boolean {
                            val oldItem = oldItems[oldItemPosition]
                            val newItem = items[newItemPosition]
                            return when (oldItem) {
                                is ItemButton -> newItem is ItemButton && oldItem == newItem
                                is ItemCheckBox -> newItem is ItemCheckBox && oldItem.fieldId == newItem.fieldId
                                is ItemList -> newItem is ItemList && oldItem.fieldId == newItem.fieldId
                                is ItemText -> newItem is ItemText && oldItem.fieldId == newItem.fieldId
                            }
                        }

                        override fun areContentsTheSame(
                            oldItemPosition: Int,
                            newItemPosition: Int
                        ): Boolean = true
                    }
                ).dispatchUpdatesTo(this)
            }
            else -> notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(messageAgentText: UsedeskMessageAgentText) {
        if (messageId != messageAgentText.id) {
            this.messageId = messageAgentText.id
            adapterScope.cancel()
            this.form = null
            this.buttons = messageAgentText.buttons
            onUpdate(null)
            if (messageAgentText.fieldsInfo.isNotEmpty()) {
                adapterScope = CoroutineScope(lifecycleScope.coroutineContext + Job())
                viewModel.modelFlow.onEach { model ->
                    val form = model.formMap[messageAgentText.id]
                    if (form != this.form) {
                        onUpdate(form)
                    }
                }.launchIn(adapterScope)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        chatR.layout.usedesk_chat_message_item_button -> ButtonViewHolder(
            inflateItem(
                parent,
                chatR.layout.usedesk_chat_message_item_button,
                chatR.style.Usedesk_Chat_Message_Text_Button,
                ::ButtonBinding
            ),
            viewModel::onEvent
        )
        chatR.layout.usedesk_chat_message_item_text -> TextViewHolder(
            inflateItem(
                parent,
                chatR.layout.usedesk_chat_message_item_text,
                chatR.style.Usedesk_Chat_Message_Text_Field_Text,
                ::TextBinding
            ),
            viewModel::onEvent
        )
        chatR.layout.usedesk_chat_message_item_checkbox -> CheckBoxViewHolder(
            inflateItem(
                parent,
                chatR.layout.usedesk_chat_message_item_checkbox,
                chatR.style.Usedesk_Chat_Message_Text_Field_CheckBox,
                ::CheckBoxBinding
            ),
            viewModel::onEvent
        )
        chatR.layout.usedesk_chat_message_item_itemlist -> ItemListViewHolder(
            inflateItem(
                parent,
                chatR.layout.usedesk_chat_message_item_itemlist,
                chatR.style.Usedesk_Chat_Message_Text_Field_List,
                ::ItemListBinding
            ),
            viewModel::onEvent
        )
        else -> throw RuntimeException("Unknown view type: $viewType")
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is ItemButton -> chatR.layout.usedesk_chat_message_item_button
        is ItemText -> chatR.layout.usedesk_chat_message_item_text
        is ItemCheckBox -> chatR.layout.usedesk_chat_message_item_checkbox
        is ItemList -> chatR.layout.usedesk_chat_message_item_itemlist
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int
    ) {
        val item = items[position]
        holder.bind(
            messageId,
            item,
            adapterScope,
            viewModel.modelFlow
        )
    }

    override fun getItemCount() = items.size

    sealed interface Item {
        data class ItemButton(val button: Button?) : Item
        data class ItemText(val fieldId: String) : Item
        data class ItemCheckBox(val fieldId: String) : Item
        data class ItemList(val fieldId: String) : Item
    }

    internal class ButtonBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val lBtn: CardView = rootView.findViewById(chatR.id.l_btn)
        val tvTitle: TextView = rootView.findViewById(chatR.id.tv_title)
        val pbLoading: ProgressBar = rootView.findViewById(chatR.id.pb_loading)
    }

    internal class TextBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val etText: EditText = rootView.findViewById(chatR.id.et_text)
    }

    internal class CheckBoxBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvText: TextView = rootView.findViewById(chatR.id.tv_text)
        val ivChecked: ImageView = rootView.findViewById(chatR.id.iv_checked)
    }

    internal class ItemListBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvText: TextView = rootView.findViewById(chatR.id.tv_text)
        val lFrame: View = rootView.findViewById(chatR.id.l_frame)
        val lClickable: View = rootView.findViewById(chatR.id.l_clickable)
    }
}