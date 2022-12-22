package ru.usedesk.chat_gui.chat.messages

import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText
import ru.usedesk.common_gui.*

internal class FormSelectorDialog private constructor(
    screen: UsedeskFragment,
    dialogStyle: Int
) : UsedeskBottomSheetDialog(screen.requireContext(), dialogStyle) {

    private val binding: Binding
    private val notSelectedTitle = "NE VIBRANO" //TODO

    init {
        val container = screen.view as ViewGroup

        binding = inflateItem(
            layoutInflater,
            container,
            R.layout.usedesk_dialog_form_selector,
            dialogStyle,
            ::Binding
        )

        binding.npPicker.wrapSelectorWheel = false

        setContentView(binding.rootView)

        BottomSheetBehavior.from(binding.rootView.parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun update(
        formSelector: MessagesViewModel.FormSelector,
        onSelected: (UsedeskMessageAgentText.Field.List.Item?) -> Unit
    ) {
        val availableItems = formSelector.list.items.filter {
            it.parentItemsId.isEmpty() || formSelector.parentSelectedId in it.parentItemsId
        }
        val values = (listOf(notSelectedTitle) + availableItems.map { it.name }).toTypedArray()

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
            UsedeskResourceManager.getResourceId(R.style.Usedesk_Chat_Attachment_Dialog)
        )
    }

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvDone: TextView = rootView.findViewById(R.id.tv_done)
        val npPicker: NumberPicker = rootView.findViewById(R.id.np_picker)
    }
}