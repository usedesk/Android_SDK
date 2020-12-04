package ru.usedesk.chat_gui.chat.adapters

import android.view.ViewGroup
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.UsedeskBottomSheetDialog
import ru.usedesk.chat_gui.databinding.UsedeskDialogOfflineFormSuccessfullyBinding
import ru.usedesk.common_gui.inflateBinding

class UsedeskOfflineFormSuccessDialog(
        container: ViewGroup
) : UsedeskBottomSheetDialog(container.context, R.style.Usedesk_Chat_Offline_Form_Success_Dialog) {

    init {
        inflateBinding<UsedeskDialogOfflineFormSuccessfullyBinding>(layoutInflater,
                container,
                R.layout.usedesk_dialog_offline_form_successfully,
                defaultStyleId).apply {

            setContentView(root)

            tvClose.setOnClickListener {
                dismiss()
            }
        }
    }
}