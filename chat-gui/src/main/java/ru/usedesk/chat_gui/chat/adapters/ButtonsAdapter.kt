package ru.usedesk.chat_gui.chat.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_sdk.entity.UsedeskMessageButton
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem

internal class ButtonsAdapter(
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
        return ButtonViewHolder(inflateItem(parent,
                R.layout.usedesk_item_chat_button,
                R.style.Usedesk_Chat_Message) { rootView, defaultStyleId ->
            ButtonBinding(rootView, defaultStyleId)
        })
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        holder.bind(buttons[position])
    }

    override fun getItemCount() = buttons.size

    inner class ButtonViewHolder(
            private val binding: ButtonBinding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        fun bind(button: UsedeskMessageButton) {
            binding.tvTitle.text = button.text
            binding.lClickable.setOnClickListener {
                onClick(button)
            }
        }
    }

    internal class ButtonBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val lClickable: ViewGroup = rootView.findViewById(R.id.l_clickable)
    }
}