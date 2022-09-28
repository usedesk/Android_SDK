package ru.usedesk.chat_gui.chat.offlineform

import android.text.InputType
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.offlineform._entity.OfflineFormItem
import ru.usedesk.chat_gui.chat.offlineform._entity.OfflineFormList
import ru.usedesk.chat_gui.chat.offlineform._entity.OfflineFormText
import ru.usedesk.common_gui.UsedeskCommonFieldListAdapter
import ru.usedesk.common_gui.UsedeskCommonFieldTextAdapter
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.onEachWithOld

internal class OfflineFormFieldsAdapter(
    recyclerView: RecyclerView,
    binding: OfflineFormPage.Binding,
    private val viewModel: OfflineFormViewModel,
    lifecycleCoroutineScope: LifecycleCoroutineScope,
    private val onListFieldClick: (String) -> Unit
) : RecyclerView.Adapter<OfflineFormFieldsAdapter.BaseViewHolder<*>>() {

    private val textFieldStyle =
        binding.styleValues.getStyle(R.attr.usedesk_chat_screen_offline_form_text_field)
    private val listFieldStyle =
        binding.styleValues.getStyle(R.attr.usedesk_chat_screen_offline_form_list_field)

    private var items = listOf<OfflineFormItem>()

    init {
        recyclerView.run {
            adapter = this@OfflineFormFieldsAdapter
            layoutManager = LinearLayoutManager(recyclerView.context)
            itemAnimator = null
        }
        viewModel.modelFlow.onEachWithOld(lifecycleCoroutineScope) { old, new ->
            if (old?.allFields != new.allFields) {
                val oldItems = items
                val newItems = new.allFields
                items = newItems

                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize() = oldItems.size

                    override fun getNewListSize() = newItems.size

                    override fun areItemsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        val oldItem = oldItems[oldItemPosition]
                        val newItem = newItems[newItemPosition]
                        return oldItem.key == newItem.key
                    }

                    override fun areContentsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        val oldItem = oldItems[oldItemPosition]
                        val newItem = newItems[newItemPosition]
                        return oldItem.title != newItem.title &&
                                (oldItem as? OfflineFormList)?.selected ==
                                (newItem as? OfflineFormList)?.selected
                    }
                }).dispatchUpdatesTo(this)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.usedesk_item_field_text -> TextViewHolder(
            inflateItem(
                parent,
                R.layout.usedesk_item_field_text,
                textFieldStyle,
                UsedeskCommonFieldTextAdapter::Binding
            )
        )
        R.layout.usedesk_item_field_list -> ListViewHolder(
            inflateItem(
                parent,
                R.layout.usedesk_item_field_list,
                listFieldStyle,
                UsedeskCommonFieldListAdapter::Binding
            )
        )
        else -> throw RuntimeException("Unknown list type")
    }

    override fun onBindViewHolder(holderText: BaseViewHolder<*>, position: Int) {
        val item = items[position]
        when (holderText) {
            is ListViewHolder -> holderText.bind(item as OfflineFormList)
            is TextViewHolder -> holderText.bind(item as OfflineFormText)
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is OfflineFormList -> R.layout.usedesk_item_field_list
        is OfflineFormText -> R.layout.usedesk_item_field_text
    }

    inner class TextViewHolder(
        binding: UsedeskCommonFieldTextAdapter.Binding
    ) : BaseViewHolder<OfflineFormText>(binding.rootView) {
        private val adapter = UsedeskCommonFieldTextAdapter(binding)
        private var previousItem: OfflineFormText? = null

        override fun bind(item: OfflineFormText) {
            if (previousItem?.key != item.key) {
                previousItem = item
                adapter.run {
                    binding.etText.run {
                        isSingleLine = item.key != OfflineFormViewModel.MESSAGE_KEY
                        inputType = InputType.TYPE_CLASS_TEXT or when (item.key) {
                            OfflineFormViewModel.NAME_KEY ->
                                InputType.TYPE_TEXT_FLAG_CAP_WORDS
                            OfflineFormViewModel.EMAIL_KEY ->
                                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                            OfflineFormViewModel.MESSAGE_KEY ->
                                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
                            else -> InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        }
                    }
                    setTitle(item.title, item.required)
                    setText(item.text)
                    setTextChangeListener { viewModel.onTextFieldChanged(item.key, it) }
                }
            }
        }
    }

    inner class ListViewHolder(
        val binding: UsedeskCommonFieldListAdapter.Binding
    ) : BaseViewHolder<OfflineFormList>(binding.rootView) {
        private val adapter = UsedeskCommonFieldListAdapter(binding)

        override fun bind(item: OfflineFormList) {
            adapter.run {
                setTitle(item.title, item.required)
                setText(
                    item.items.getOrNull(item.selected) ?: binding.rootView.resources.getString(
                        R.string.usedesk_not_selected
                    )
                )
                setOnClickListener { onListFieldClick(item.key) }
            }
        }
    }

    sealed class BaseViewHolder<ITEM>(rootView: View) : RecyclerView.ViewHolder(rootView) {
        abstract fun bind(item: ITEM)
    }
}