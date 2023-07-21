package ru.usedesk.chat_gui.chat.messages

import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskBottomSheetDialog
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.common_gui.inflateItem

internal class FormSelectorDialog private constructor(
    screen: UsedeskFragment,
    dialogStyle: Int
) : UsedeskBottomSheetDialog(screen.requireContext(), dialogStyle) {

    private val binding: Binding

    init {
        binding = inflateItem(
            layoutInflater,
            screen.view as ViewGroup,
            R.layout.usedesk_dialog_form_selector,
            dialogStyle,
            ::Binding
        ).apply {
            npPicker.wrapSelectorWheel = false

            setContentView(rootView)
        }
    }

    fun update(
        formSelector: MessagesViewModel.FormSelector,
        onSelected: (UsedeskForm.Field.List.Item?) -> Unit
    ) {
        val availableItems = formSelector.list.items.filter {
            it.parentItemsId.isEmpty() || formSelector.parentSelectedId in it.parentItemsId
        }
        val values = arrayOf(binding.styleValues.getString(R.attr.usedesk_text_1)) +
                availableItems.map(UsedeskForm.Field.List.Item::name)

        binding.npPicker.apply {
            minValue = 0
            maxValue = 0
            value = 0
            displayedValues = values
            maxValue = values.size - 1
            value = availableItems.indexOfFirst { it.id == formSelector.list.selected?.id } + 1
        }

        setOnDismissListener {
            onSelected(formSelector.list.selected)
        }

        binding.tvDone.setOnClickListener {
            setOnDismissListener(null)
            dismiss()
            onSelected(
                when (val index = binding.npPicker.value) {
                    0 -> null
                    else -> availableItems[index - 1]
                }
            )
        }
    }

    companion object {
        fun create(screen: UsedeskFragment) = FormSelectorDialog(
            screen,
            UsedeskResourceManager.getResourceId(R.style.Usedesk_Chat_FormSelector_Dialog)
        )
    }

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvDone: TextView = rootView.findViewById(R.id.tv_done)
        val npPicker: NumberPicker = rootView.findViewById(R.id.np_picker)
    }
}