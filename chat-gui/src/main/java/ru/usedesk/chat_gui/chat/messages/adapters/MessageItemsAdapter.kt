package ru.usedesk.chat_gui.chat.messages.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
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
    private var items: List<Item> = listOf()

    init {
        recyclerView.adapter = this
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(messageId: Long, newItems: List<Item>) {
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
        else -> throw RuntimeException("Unknown view type: $viewType")
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is Item.Button -> R.layout.usedesk_chat_message_item_button
        is Item.Field.Text -> R.layout.usedesk_chat_message_item_text
        else -> 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    internal abstract class BaseViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {

        abstract fun bind(item: Item)
    }

    inner class ButtonViewHolder(private val binding: ButtonBinding) :
        BaseViewHolder(binding.rootView) {

        override fun bind(item: Item) {
            val button = item as Item.Button
            binding.tvTitle.run {
                text = button.text
                setOnClickListener { onClick(button) }
            }
        }
    }

    inner class TextViewHolder(private val binding: TextBinding) :
        BaseViewHolder(binding.rootView) {

        override fun bind(item: Item) {
            val text = item as Item.Field.Text
            binding.etText.run {
                setText(text.text)
                //setOnClickListener { onClick(text) }
            }
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
}