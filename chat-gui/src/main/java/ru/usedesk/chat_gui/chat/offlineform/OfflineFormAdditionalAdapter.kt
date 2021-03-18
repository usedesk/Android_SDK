package ru.usedesk.chat_gui.chat.offlineform

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.common_gui.UsedeskCommonFieldListAdapter
import ru.usedesk.common_gui.UsedeskCommonFieldTextAdapter
import ru.usedesk.common_gui.inflateItem

internal class OfflineFormAdditionalAdapter(
        recyclerView: RecyclerView,
        private val viewModel: OfflineFormViewModel,
        private val onSubjectClick: () -> Unit
) : RecyclerView.Adapter<OfflineFormAdditionalAdapter.BaseViewHolder>() {

    private var settings: UsedeskOfflineFormSettings
    private var items = listOf<UsedeskOfflineFormSettings.CustomField>()

    init {
        recyclerView.adapter = this
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
    }

    fun init(customFields: List<UsedeskOfflineFormSettings.CustomField>) {
        this.items = customFields
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return TextViewHolder(inflateItem(parent,
                R.layout.usedesk_item_field_text,
                R.style.Usedesk_Chat_Screen_Offline_Form_Additional) { rootView, defaultStyleId ->
            UsedeskCommonFieldTextAdapter.Binding(rootView, defaultStyleId)
        })
    }

    override fun onBindViewHolder(holderText: BaseViewHolder, position: Int) {
        holderText.bind(position)
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return if (position == 2) {
            TYPE_LIST
        } else {
            TYPE_TEXT
        }
    }

    inner class TextViewHolder(
            binding: UsedeskCommonFieldTextAdapter.Binding
    ) : BaseViewHolder(binding.rootView) {
        private val adapter = UsedeskCommonFieldTextAdapter(binding)

        override fun bind(index: Int) {
            when (index) {
                0 -> {
                    adapter.setText(viewModel.configuration.clientName)
                    adapter.setTextChangeListener {
                        viewModel.onOfflineFormNameChanged(it)
                    }
                }
                1 -> {
                    adapter.setText(viewModel.configuration.clientEmail)
                    adapter.setTextChangeListener {
                        viewModel.onOfflineFormEmailChanged(it)
                    }
                }

                messageAdapter.apply {
                    setTextChangeListener {
                        viewModel.onOfflineFormMessageChanged(it)
                    }
                }
            }
            /*adapter.setTitle(customField.placeholder, customField.required)
            adapter.setTextChangeListener {
                viewModel.onOfflineFormAdditionalChanged(index, it)
            }*/
        }
    }

    inner class ListViewHolder(
            binding: UsedeskCommonFieldListAdapter.Binding
    ) : BaseViewHolder(binding.rootView) {
        private val adapter = UsedeskCommonFieldListAdapter(binding)

        override fun bind(index: Int) {
            adapter.setOnClickListener {
                onSubjectClick()
            }
            /*adapter.setTitle(customField.placeholder, customField.required)
            adapter.setTextChangeListener {
                viewModel.onOfflineFormAdditionalChanged(index, it)
            }*/
        }
    }

    abstract inner class BaseViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {

        abstract fun bind(index: Int)
    }

    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_LIST = 2
    }
}