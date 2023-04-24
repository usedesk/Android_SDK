
package ru.usedesk.chat_gui.chat.offlineform

import android.text.InputType
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.offlineform.OfflineFormViewModel.Companion.EMAIL_KEY
import ru.usedesk.chat_gui.chat.offlineform.OfflineFormViewModel.Companion.MESSAGE_KEY
import ru.usedesk.chat_gui.chat.offlineform.OfflineFormViewModel.Companion.NAME_KEY
import ru.usedesk.chat_gui.chat.offlineform.OfflineFormViewModel.Model.OfflineFormItem
import ru.usedesk.common_gui.UsedeskCommonFieldListAdapter
import ru.usedesk.common_gui.UsedeskCommonFieldTextAdapter
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.onEachWithOld

internal class OfflineFormFieldsAdapter(
    recyclerView: RecyclerView,
    binding: OfflineFormPage.Binding,
    private val viewModel: OfflineFormViewModel,
    private val coroutineScope: LifecycleCoroutineScope,
    private val onListFieldClick: (String) -> Unit
) : RecyclerView.Adapter<OfflineFormFieldsAdapter.BaseViewHolder<*>>() {

    private val nameTitle =
        binding.styleValues.getString(R.attr.usedesk_chat_screen_offline_form_name)
    private val emailTitle =
        binding.styleValues.getString(R.attr.usedesk_chat_screen_offline_form_email)
    private val messageTitle =
        binding.styleValues.getString(R.attr.usedesk_chat_screen_offline_form_message)

    private val commonError =
        binding.styleValues.getString(R.attr.usedesk_chat_screen_offline_form_field_error)
    private val emailError =
        binding.styleValues.getString(R.attr.usedesk_chat_screen_offline_form_email_error)

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
        viewModel.modelFlow.onEachWithOld(coroutineScope) { old, new ->
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
                                (oldItem as? OfflineFormItem.List)?.selected ==
                                (newItem as? OfflineFormItem.List)?.selected
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
            is ListViewHolder -> holderText.bind(item as OfflineFormItem.List)
            is TextViewHolder -> holderText.bind(item as OfflineFormItem.Text)
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is OfflineFormItem.List -> R.layout.usedesk_item_field_list
        is OfflineFormItem.Text -> R.layout.usedesk_item_field_text
    }

    inner class TextViewHolder(
        binding: UsedeskCommonFieldTextAdapter.Binding
    ) : BaseViewHolder<OfflineFormItem.Text>(binding.rootView) {
        private val adapter = UsedeskCommonFieldTextAdapter(binding)
        private var previousItem: OfflineFormItem.Text? = null
        private var itemScope: CoroutineScope? = null

        override fun bind(item: OfflineFormItem.Text) {
            if (previousItem?.key != item.key) {
                itemScope?.cancel()
                previousItem = item
                adapter.run {
                    binding.etText.run {
                        isSingleLine = item.key != MESSAGE_KEY
                        inputType = InputType.TYPE_CLASS_TEXT or when (item.key) {
                            NAME_KEY -> InputType.TYPE_TEXT_FLAG_CAP_WORDS
                            EMAIL_KEY -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                            MESSAGE_KEY -> InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                                    InputType.TYPE_TEXT_FLAG_MULTI_LINE
                            else -> InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        }
                    }
                    setTitle(
                        title = when (item.key) {
                            NAME_KEY -> nameTitle
                            EMAIL_KEY -> emailTitle
                            MESSAGE_KEY -> messageTitle
                            else -> item.title
                        },
                        required = item.required
                    )
                    setText(item.text)
                    setTextChangeListener { viewModel.onTextFieldChanged(item.key, it) }
                }
                CoroutineScope(coroutineScope.coroutineContext + Job()).apply {
                    itemScope = this
                    launch {
                        var oldState: OfflineFormViewModel.Model? = null
                        viewModel.modelFlow.collect { state ->
                            if (oldState != state) {
                                if (oldState?.fieldFocus != state.fieldFocus &&
                                    state.fieldFocus?.data == item.key
                                ) {
                                    state.fieldFocus.use {
                                        adapter.binding.etText.run {
                                            postDelayed({
                                                clearFocus()
                                                requestFocus()
                                            }, 100)
                                        }
                                    }
                                }
                                if (oldState?.allFields != state.allFields) {
                                    val field = state.allFields.firstOrNull { it.key == item.key }
                                            as? OfflineFormItem.Text
                                    if (field != null) {
                                        adapter.showError(
                                            when {
                                                field.error -> when (item.key) {
                                                    EMAIL_KEY -> emailError
                                                    else -> commonError
                                                }
                                                else -> null
                                            }
                                        )
                                    }
                                }
                                oldState = state
                            }
                        }
                    }
                }
            }
        }
    }

    inner class ListViewHolder(
        val binding: UsedeskCommonFieldListAdapter.Binding
    ) : BaseViewHolder<OfflineFormItem.List>(binding.rootView) {
        private val adapter = UsedeskCommonFieldListAdapter(binding)

        override fun bind(item: OfflineFormItem.List) {
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