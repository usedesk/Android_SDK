package ru.usedesk.chat_gui.chat.adapters

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.ChatViewModel
import ru.usedesk.common_gui.IUsedeskAdapter
import ru.usedesk.common_gui.inflateItem

internal class AdditionalAdapter(
        recyclerView: RecyclerView
) : RecyclerView.Adapter<AdditionalAdapter.ViewHolder>(), IUsedeskAdapter<ChatViewModel> {

    init {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(recyclerView.context)
            adapter = this@AdditionalAdapter
            isNestedScrollingEnabled = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflateItem(parent,
                R.layout.usedesk_item_field_text,
                R.style.Usedesk_Chat_Screen_Offline_Form_Additional) { rootView, defaultStyleId ->
            UsedeskCommonFieldTextAdapter.Binding(rootView, defaultStyleId)
        })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 3

    override fun onLiveData(viewModel: ChatViewModel, lifecycleOwner: LifecycleOwner) {
        notifyDataSetChanged()
    }

    internal class ViewHolder(
            binding: UsedeskCommonFieldTextAdapter.Binding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        private val adapter = UsedeskCommonFieldTextAdapter(binding)

        fun bind() {

        }
    }
}