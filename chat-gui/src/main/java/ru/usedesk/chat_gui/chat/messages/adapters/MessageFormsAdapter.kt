package ru.usedesk.chat_gui.chat.messages.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
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
    private val pbLoading: ProgressBar, //TODO:button
    private val viewModel: MessagesViewModel,
    private val lifecycleScope: CoroutineScope
) : RecyclerView.Adapter<BaseViewHolder>() {

    private var adapterScope = CoroutineScope(Dispatchers.Main)
    private var messageId: Long = 0L
    private var items = listOf<Item>()
    private var buttons = listOf<Button>()
    private var form: UsedeskForm? = null
    private val applyTitle = "АПЛАЙ" //TODO

    init {
        recyclerView.adapter = this
    }

    sealed interface Item {
        data class ItemButton(val button: Button) : Item
        data class ItemText(val fieldId: String) : Item
        data class ItemCheckBox(val fieldId: String) : Item
        data class ItemList(val fieldId: String) : Item
    }

    private fun getItems(
        buttons: List<Button>,
        form: UsedeskForm
    ) = buttons.map { ItemButton(it) } + when (form.state) {
        UsedeskForm.State.NOT_LOADED,
        UsedeskForm.State.LOADING -> listOf()
        else -> form.fields.map { field ->
            when (field) {
                is Field.CheckBox -> ItemCheckBox(field.id)
                is Field.List -> ItemList(field.id)
                is Field.Text -> ItemText(field.id)
            }
        }
    }

    private fun onUpdate(form: UsedeskForm) {
        val oldForm = this.form
        when (oldForm?.id) {
            form.id -> if (oldForm.state != form.state) {
                val oldItems = items
                this.form = form
                items = getItems(buttons, form)
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
            else -> {
                this.form = form
                items = getItems(buttons, form)
                notifyDataSetChanged()
            }
        }
        recyclerView.visibility = visibleGone(
            when (form.state) {
                UsedeskForm.State.NOT_LOADED,
                UsedeskForm.State.LOADING -> false
                else -> true
            }
        )
        /*
            val loading = state == UsedeskForm.State.SENDING
            isEnabled = !loading
            isClickable = !loading
            isFocusable = !loading
            text = applyTitle
            setOnClickListener { onEvent(Event.FormApplyClick(messageId)) }
            binding.pbLoading.visibility = visibleInvisible(loading)
         */
        pbLoading.visibility = visibleGone(form.state == UsedeskForm.State.LOADING)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(
        formId: Long,
        buttons: List<Button>
    ) {
        if (messageId != formId) {
            this.messageId = formId
            adapterScope.cancel()
            if (formId != 0L) {
                adapterScope = CoroutineScope(lifecycleScope.coroutineContext + Job())
                this.form = null
                this.buttons = buttons
                viewModel.modelFlow.onEach { model ->
                    val form = model.formMap[formId]
                    if (form != null && form != this.form) {
                        onUpdate(form)
                    }
                }.launchIn(adapterScope)
            }
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
            viewModel::onEvent
        )
        R.layout.usedesk_chat_message_item_text -> TextViewHolder(
            inflateItem(
                parent,
                R.layout.usedesk_chat_message_item_text,
                R.style.Usedesk_Chat_Message_Text_Button,
                ::TextBinding
            ),
            viewModel::onEvent
        )
        R.layout.usedesk_chat_message_item_checkbox -> CheckBoxViewHolder(
            inflateItem(
                parent,
                R.layout.usedesk_chat_message_item_checkbox,
                R.style.Usedesk_Chat_Message_Text_Button,
                ::CheckBoxBinding
            ),
            viewModel::onEvent
        )
        R.layout.usedesk_chat_message_item_itemlist -> ItemListViewHolder(
            inflateItem(
                parent,
                R.layout.usedesk_chat_message_item_itemlist,
                R.style.Usedesk_Chat_Message_Text_ItemList,
                ::ItemListBinding
            ),
            viewModel::onEvent
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
            adapterScope,
            viewModel.modelFlow
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
        val lClickable: View = rootView.findViewById(R.id.l_clickable)
        val ivChecked: ImageView = rootView.findViewById(R.id.iv_checked)
    }

    internal class ItemListBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvText: TextView = rootView.findViewById(R.id.tv_text)
        val lFrame: View = rootView.findViewById(R.id.l_frame)
        val lClickable: View = rootView.findViewById(R.id.l_clickable)
    }
}