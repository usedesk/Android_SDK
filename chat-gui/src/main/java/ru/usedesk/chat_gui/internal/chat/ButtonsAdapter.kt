package ru.usedesk.chat_gui.internal.chat

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.databinding.UsedeskItemChatButtonBinding
import ru.usedesk.chat_sdk.external.entity.UsedeskMessageButton
import ru.usedesk.common_gui.internal.inflateItem

class ButtonsAdapter(
        recyclerView: RecyclerView,
        private val onClick: (UsedeskMessageButton) -> Unit
) : RecyclerView.Adapter<ButtonsAdapter.ButtonViewHolder>() {

    private var buttons: List<UsedeskMessageButton> = listOf()

    init {
        recyclerView.adapter = this
    }

    fun update(buttons: List<UsedeskMessageButton>) {
        this.buttons = buttons
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        return ButtonViewHolder(inflateItem(R.layout.usedesk_item_chat_button, parent))
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        holder.bind(buttons[position])
    }

    override fun getItemCount() = buttons.size

    inner class ButtonViewHolder(
            private val binding: UsedeskItemChatButtonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(button: UsedeskMessageButton) {
            binding.tvTitle.text = button.text
            binding.lClickable.setOnClickListener {
                onClick(button)
            }
        }
    }
}