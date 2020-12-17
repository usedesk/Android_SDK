package ru.usedesk.chat_gui.chat.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.UsedeskBottomSheetDialog
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.common_gui.inflateItem

class UsedeskOfflineFormSuccessDialog private constructor(
        container: ViewGroup,
        dialogStyle: Int
) : UsedeskBottomSheetDialog(container.context, dialogStyle) {

    init {
        inflateItem(layoutInflater,
                container,
                R.layout.usedesk_dialog_offline_form_successfully) {
            Binding(it)
        }.apply {
            setContentView(rootView)

            tvClose.setOnClickListener {
                dismiss()
            }
        }
    }

    companion object {
        fun create(container: View): UsedeskOfflineFormSuccessDialog {
            val dialogStyle = UsedeskResourceManager.getStyleValues(
                    container.context,
                    R.style.Usedesk_Chat
            ).getStyle(R.attr.usedesk_chat_offline_form_success_dialog_style)
            return UsedeskOfflineFormSuccessDialog(container as ViewGroup, dialogStyle)
        }
    }

    class Binding(rootView: View) : UsedeskBinding(rootView) {
        val tvClose: TextView = rootView.findViewById(R.id.tv_close)
    }
}