package ru.usedesk.chat_gui.chat.messages.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.AgentItem
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.ItemState
import ru.usedesk.chat_gui.chat.messages.adapters.MessageItemsAdapter.BaseViewHolder
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Item
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem

//TODO: вытащить вьюхолдеры во вне
internal class MessageItemsAdapter(
    recyclerView: RecyclerView,
    private val onClick: (Item.Button) -> Unit
) : RecyclerView.Adapter<BaseViewHolder>() {

    private var messageId = 0L
    private var items: List<AgentItem<*, *>> = listOf()

    init {
        recyclerView.adapter = this
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(messageId: Long, newItems: List<AgentItem<*, *>>) {
        val oldItems = items
        items = newItems
        when (messageId) {
            /*this.messageId -> {
                //TODO: DiffUtil??
            }*/
            else -> {
                this.messageId = messageId
                notifyDataSetChanged()
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
            )
        )
        R.layout.usedesk_chat_message_item_text -> TextViewHolder(
            inflateItem(
                parent,
                R.layout.usedesk_chat_message_item_text,
                R.style.Usedesk_Chat_Message_Text_Button,
                ::TextBinding
            )
        )
        R.layout.usedesk_chat_message_item_checkbox -> CheckBoxViewHolder(
            inflateItem(
                parent,
                R.layout.usedesk_chat_message_item_checkbox,
                R.style.Usedesk_Chat_Message_Text_Button,
                ::CheckBoxBinding
            )
        )
        R.layout.usedesk_chat_message_item_itemlist -> ItemListViewHolder(
            inflateItem(
                parent,
                R.layout.usedesk_chat_message_item_itemlist,
                R.style.Usedesk_Chat_Message_Text_ItemList,
                ::ItemListBinding
            )
        )
        else -> throw RuntimeException("Unknown view type: $viewType")
    }

    override fun getItemViewType(position: Int) = when (items[position].item) {
        is Item.Button -> R.layout.usedesk_chat_message_item_button
        is Item.Field.Text -> R.layout.usedesk_chat_message_item_text
        is Item.Field.CheckBox -> R.layout.usedesk_chat_message_item_checkbox
        is Item.Field.ItemList -> R.layout.usedesk_chat_message_item_itemlist
        else -> 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    internal abstract class BaseViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {

        abstract fun bind(agentItem: AgentItem<*, *>)
    }

    inner class ButtonViewHolder(private val binding: ButtonBinding) :
        BaseViewHolder(binding.rootView) {

        override fun bind(agentItem: AgentItem<*, *>) {
            agentItem as AgentItem<Item.Button, ItemState.Button>
            binding.tvTitle.run {
                text = agentItem.item.name
                isEnabled = agentItem.state.enabled
                isClickable = agentItem.state.enabled
                isFocusable = agentItem.state.enabled
                setOnClickListener(when {
                    agentItem.state.enabled -> {
                        { onClick(agentItem.item) }
                    }
                    else -> null
                })
            }
        }
    }

    inner class TextViewHolder(private val binding: TextBinding) :
        BaseViewHolder(binding.rootView) {

        override fun bind(agentItem: AgentItem<*, *>) {
            agentItem as AgentItem<Item.Field.Text, ItemState.Text>
            binding.etText.run {
                hint = agentItem.item.name
                setText(agentItem.state.text)
            }
        }
    }

    inner class CheckBoxViewHolder(private val binding: CheckBoxBinding) :
        BaseViewHolder(binding.rootView) {

        override fun bind(agentItem: AgentItem<*, *>) {
            agentItem as AgentItem<Item.Field.CheckBox, ItemState.CheckBox>
            binding.tvText.text = agentItem.item.name
        }
    }

    inner class ItemListViewHolder(private val binding: ItemListBinding) :
        BaseViewHolder(binding.rootView) {

        override fun bind(agentItem: AgentItem<*, *>) {
            agentItem as AgentItem<Item.Field.ItemList, ItemState.ItemList>
            val name = when {
                agentItem.item.items.isNotEmpty() -> agentItem.state.selected.joinToString(separator = ", ") {
                    it.name
                }.ifEmpty { null }
                else -> null
            }
            binding.tvText.text = name ?: agentItem.item.name
            //binding.tvText.setTextColor() //TODO: цвет текста
        }
    }

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