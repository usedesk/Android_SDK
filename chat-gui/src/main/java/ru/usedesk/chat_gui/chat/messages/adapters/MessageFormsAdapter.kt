package ru.usedesk.chat_gui.chat.messages.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormsAdapter.Item.*
import ru.usedesk.chat_gui.chat.messages.adapters.holders.*
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Button
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Field
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.visibleGone

//TODO: вытащить вьюхолдеры во вне
internal class MessageFormsAdapter(
    private val recyclerView: RecyclerView,
    private val pbLoading: ProgressBar,
    private val onEvent: (Event) -> Unit
) : RecyclerView.Adapter<BaseViewHolder>() {

    private var adapterScope = CoroutineScope(Dispatchers.Main)
    private var messageId: Long = 0L
    private var items = listOf<Item>()
    private var buttons = listOf<Button>()
    private var form: UsedeskForm? = null

    init {
        recyclerView.adapter = this
    }

    sealed interface Item {
        data class ItemButton(val button: Button?) : Item
        data class ItemText(val text: Field.Text) : Item
        data class ItemCheckBox(val checkBox: Field.CheckBox) : Item
        data class ItemList(val list: Field.List) : Item
    }

    private fun getItems(
        buttons: List<Button>,
        form: UsedeskForm
    ) = buttons.map { ItemButton(it) } + when (form.state) {
        UsedeskForm.State.NOT_LOADED,
        UsedeskForm.State.LOADING -> listOf()
        else -> form.fields.map {
            when (it) {
                is Field.CheckBox -> ItemCheckBox(it)
                is Field.List -> ItemList(it)
                is Field.Text -> ItemText(it)
            }
        } + when (form.state) {
            UsedeskForm.State.LOADED,
            UsedeskForm.State.SENDING -> listOf(ItemButton(null))
            else -> listOf()
        }
    }

    private fun onUpdate(form: UsedeskForm) {
        if (this.form?.state != form.state) {
            val oldForm = this.form
            val oldItems = items
            this.form = form
            items = getItems(buttons, form)
            when {
                oldForm?.id == form.id && oldForm.state == form.state -> DiffUtil.calculateDiff(
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
                                is ItemCheckBox -> newItem is ItemCheckBox && oldItem.checkBox.id == newItem.checkBox.id
                                is ItemList -> newItem is ItemList && oldItem.list.id == newItem.list.id
                                is ItemText -> newItem is ItemText && oldItem.text.id == newItem.text.id
                            }
                        }

                        override fun areContentsTheSame(
                            oldItemPosition: Int,
                            newItemPosition: Int
                        ): Boolean {
                            val oldItem = oldItems[oldItemPosition]
                            val newItem = items[newItemPosition]
                            return when (oldItem) {
                                is ItemButton -> newItem is ItemButton
                                is ItemCheckBox -> newItem is ItemCheckBox
                                is ItemList -> newItem is ItemList && oldItem.list.selected == newItem.list.selected
                                is ItemText -> newItem is ItemText
                            }
                        }
                    }
                ).dispatchUpdatesTo(this)
                else -> notifyDataSetChanged()
            }
            recyclerView.visibility = visibleGone(items.isNotEmpty())
            pbLoading.visibility = visibleGone(form.state == UsedeskForm.State.LOADING)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(
        messageId: Long,
        viewModel: MessagesViewModel,
        lifecycleScope: CoroutineScope,
        buttons: List<Button>
    ) {
        if (this.messageId != messageId) {
            this.messageId = messageId
            adapterScope.cancel()
            adapterScope = CoroutineScope(lifecycleScope.coroutineContext + Job())
            this.form = UsedeskForm()
            this.buttons = buttons
            viewModel.modelFlow.onEach { model ->
                val form = model.formMap[messageId]
                if (form != null && form != this.form) {
                    onUpdate(form)
                }
            }.launchIn(adapterScope)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.usedesk_chat_message_item_button -> ButtonViewHolder(
            inflateItem(
                parent,
                R.layout.usedesk_chat_message_item_button,
                R.style.Usedesk_Chat_Message_Text_Button,
                ::ButtonBinding
            ),
            onEvent
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

    override fun getItemViewType(position: Int) = when (items[position]) {
        is ItemButton -> R.layout.usedesk_chat_message_item_button
        is ItemText -> R.layout.usedesk_chat_message_item_text
        is ItemCheckBox -> R.layout.usedesk_chat_message_item_checkbox
        is ItemList -> R.layout.usedesk_chat_message_item_itemlist
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int
    ) {
        val item = items[position]
        holder.bind(
            messageId,
            item,
            form?.state ?: UsedeskForm.State.LOADING
        )
    }

    override fun getItemCount() = items.size

    internal class ButtonBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
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