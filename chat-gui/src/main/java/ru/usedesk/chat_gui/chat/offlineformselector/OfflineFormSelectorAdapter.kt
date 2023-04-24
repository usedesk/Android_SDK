
package ru.usedesk.chat_gui.chat.offlineformselector

import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.offlineform.OfflineFormViewModel
import ru.usedesk.chat_gui.chat.offlineform.OfflineFormViewModel.Model.OfflineFormItem
import ru.usedesk.common_gui.UsedeskCommonFieldCheckBoxAdapter
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.onEachWithOld

internal class OfflineFormSelectorAdapter(
    private val key: String,
    recyclerView: RecyclerView,
    binding: OfflineFormSelectorPage.Binding,
    private val viewModel: OfflineFormViewModel,
    lifecycleCoroutineScope: LifecycleCoroutineScope,
) : RecyclerView.Adapter<OfflineFormSelectorAdapter.ViewHolder>() {

    private val itemStyle =
        binding.styleValues.getStyle(R.attr.usedesk_chat_screen_offline_form_selector_checkbox)

    private var items = listOf<String?>()
    private var selected: String? = null

    init {
        recyclerView.run {
            layoutManager = LinearLayoutManager(recyclerView.context)
            adapter = this@OfflineFormSelectorAdapter
        }
        viewModel.modelFlow.onEachWithOld(lifecycleCoroutineScope) { old, new ->
            val oldField = old?.customFields?.firstOrNull { it.key == key }
            val newField = new.customFields.first { it.key == key } as OfflineFormItem.List
            if (oldField != newField) {
                val oldItems = items
                val newItems = listOf(null) + newField.items

                val oldSelected = selected
                val newSelected = newField.items.getOrNull(newField.selected)

                items = newItems
                selected = newSelected

                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize() = oldItems.size

                    override fun getNewListSize() = newItems.size

                    override fun areItemsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        val oldItem = oldItems[oldItemPosition]
                        val newItem = newItems[newItemPosition]
                        return oldItem == newItem
                    }

                    override fun areContentsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        val oldItem = oldItems[oldItemPosition]
                        val newItem = newItems[newItemPosition]
                        return oldSelected == newSelected
                                || (oldItem != oldSelected && newItem != newSelected)
                    }
                }).dispatchUpdatesTo(this)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        inflateItem(
            parent,
            R.layout.usedesk_item_field_checkbox,
            itemStyle,
            UsedeskCommonFieldCheckBoxAdapter::Binding
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(
        private val binding: UsedeskCommonFieldCheckBoxAdapter.Binding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        private val adapter = UsedeskCommonFieldCheckBoxAdapter(binding)

        fun bind(item: String?) {
            adapter.run {
                setTitle(
                    item ?: binding.rootView.resources.getString(R.string.usedesk_not_selected)
                )
                setChecked(item == selected)
                setOnClickListener { viewModel.onListFieldChanged(key, item) }
            }
        }
    }
}